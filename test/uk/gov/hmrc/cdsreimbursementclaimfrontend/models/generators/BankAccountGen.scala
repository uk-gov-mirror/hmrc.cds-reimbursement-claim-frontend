/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cdsreimbursementclaimfrontend.models.generators

import org.scalacheck.Gen
import org.scalacheck.ScalacheckShapeless._
import uk.gov.hmrc.cdsreimbursementclaimfrontend.controllers.claims.BankAccountController.{AccountName, AccountNumber, BankAccountDetails}
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.SortCode

object BankAccountGen extends GenUtils {
  implicit val sortCodeGen: Gen[SortCode]                     = gen[SortCode]
  implicit val accountNameGen: Gen[AccountName]               = gen[AccountName]
  implicit val accountNunmberGen: Gen[AccountNumber]          = gen[AccountNumber]
  implicit val bankAccountDetailsGen: Gen[BankAccountDetails] = gen[BankAccountDetails]

}
