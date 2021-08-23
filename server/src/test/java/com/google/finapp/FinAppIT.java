/*
 * Copyright 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.finapp;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.cloud.ByteArray;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.finapp.CreateAccountRequest.Status;
import com.google.finapp.CreateAccountRequest.Type;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FinAppIT {

  private static final String DEFAULT_AMOUNT = "100";

  private DatabaseClient databaseClient;
  private FinAppGrpc.FinAppBlockingStub finAppService;

  @Before
  public void setupFinappService() {
    finAppService =
        FinAppGrpc.newBlockingStub(
            ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build());
  }

  @Before
  public void setupSpannerDao() {
    Spanner spanner = SpannerOptions.newBuilder().build().getService();
    databaseClient =
        spanner.getDatabaseClient(DatabaseId.of("test-project", "test-instance", "test-database"));
  }

  @Test
  public void createAccount_createsSingleValidAccount() throws Exception {
    CreateAccountResponse response =
        finAppService.createAccount(
            CreateAccountRequest.newBuilder()
                .setType(Type.CHECKING)
                .setStatus(Status.ACTIVE)
                .setBalance(DEFAULT_AMOUNT)
                .build());
    ByteArray accountId = ByteArray.copyFrom(response.getAccountId().toByteArray());
    try (ResultSet resultSet =
        databaseClient
            .singleUse()
            .read(
                "Account",
                KeySet.singleKey(Key.of(accountId)),
                Arrays.asList("AccountType", "AccountStatus", "Balance"))) {
      int count = 0;
      while (resultSet.next()) {
        assertThat(resultSet.getLong(0)).isEqualTo(1);
        assertThat(resultSet.getLong(1)).isEqualTo(1);
        assertThat(resultSet.getBigDecimal(2)).isEqualTo(new BigDecimal(DEFAULT_AMOUNT));
        count++;
      }
      assertThat(count).isEqualTo(1);
    }
  }

  @Test
  public void moveAccountBalance_validTransfer() throws Exception {
    ByteString fromAccountId = createAccount(/* amount= */ "100");
    ByteString toAccountId = createAccount(/* amount= */ "50");

    MoveAccountBalanceResponse response =
        finAppService.moveAccountBalance(
            MoveAccountBalanceRequest.newBuilder()
                .setAmount("10")
                .setFromAccountId(fromAccountId)
                .setToAccountId(toAccountId)
                .build());

    assertThat(response.getFromAccountIdBalance()).isEqualTo("90");
    assertThat(response.getToAccountIdBalance()).isEqualTo("60");
    ByteArray fromAccountIdBytes = ByteArray.copyFrom(fromAccountId.toByteArray());
    ByteArray toAccountIdBytes = ByteArray.copyFrom(toAccountId.toByteArray());
    try (ResultSet resultSet =
        databaseClient
            .singleUse()
            .read(
                "Account",
                KeySet.newBuilder()
                    .addKey(Key.of(fromAccountIdBytes))
                    .addKey(Key.of(toAccountIdBytes))
                    .build(),
                Arrays.asList("AccountId", "Balance"))) {
      int count = 0;
      while (resultSet.next()) {
        if (resultSet.getBytes(0).equals(fromAccountIdBytes)) {
          assertThat(resultSet.getBigDecimal(1)).isEqualTo(BigDecimal.valueOf(90));
          count++;
        } else if (resultSet.getBytes(0).equals(toAccountIdBytes)) {
          assertThat(resultSet.getBigDecimal(1)).isEqualTo(BigDecimal.valueOf(60));
          count++;
        }
      }
      assertThat(count).isEqualTo(2);
    }
  }

  @Test
  public void moveAccountBalance_negativeAmount_throwsException() throws Exception {
    ByteString fromAccountId = createAccount(DEFAULT_AMOUNT);
    ByteString toAccountId = createAccount(DEFAULT_AMOUNT);

    StatusRuntimeException expected =
        assertThrows(
            StatusRuntimeException.class,
            () ->
                finAppService.moveAccountBalance(
                    MoveAccountBalanceRequest.newBuilder()
                        .setAmount("-10")
                        .setFromAccountId(fromAccountId)
                        .setToAccountId(toAccountId)
                        .build()));
    assertThat(expected).hasMessageThat().contains("cannot be negative");
  }

  @Test
  public void moveAccountBalance_tooLargeAmount_throwsException() throws Exception {
    ByteString fromAccountId = createAccount(/* amount= */ "100");
    ByteString toAccountId = createAccount(DEFAULT_AMOUNT);

    StatusRuntimeException expected =
        assertThrows(
            StatusRuntimeException.class,
            () ->
                finAppService.moveAccountBalance(
                    MoveAccountBalanceRequest.newBuilder()
                        .setAmount("200")
                        .setFromAccountId(fromAccountId)
                        .setToAccountId(toAccountId)
                        .build()));
    assertThat(expected).hasMessageThat().contains("greater than original balance");
  }

  private ByteString createAccount(String amount) throws StatusException {
    return finAppService
        .createAccount(
            CreateAccountRequest.newBuilder()
                .setType(Type.CHECKING)
                .setStatus(Status.ACTIVE)
                .setBalance(amount)
                .build())
        .getAccountId();
  }
}
