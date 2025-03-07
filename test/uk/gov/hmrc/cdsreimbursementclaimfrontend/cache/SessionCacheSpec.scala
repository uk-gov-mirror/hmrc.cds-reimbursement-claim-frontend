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

package uk.gov.hmrc.cdsreimbursementclaimfrontend.cache

import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.Configuration
import play.api.libs.json.{JsNumber, JsObject}
import play.api.test.Helpers._
import uk.gov.hmrc.cache.model.Id
import uk.gov.hmrc.cdsreimbursementclaimfrontend.cache.SessionCacheSpec.{TestEnvironment, config}
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models._
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.generators.Generators.{arb, sample}
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.generators.SessionDataGen._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.mongo.DatabaseUpdate

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionCacheSpec
    extends AnyWordSpec
    with Matchers
    with MongoSupport
    with Eventually
    with ScalaCheckDrivenPropertyChecks {

  val sessionStore = new DefaultSessionCache(reactiveMongoComponent, config)

  "SessionCache" must {

    "be able to insert SessionData into mongo and read it back" in new TestEnvironment {
      forAll { sessionData: SessionData =>
        val result = sessionStore.store(sessionData)

        await(result) should be(Right(()))

        eventually {
          val getResult = sessionStore.get()
          await(getResult) should be(Right(Some(sessionData)))
        }
      }
    }

    "return no SessionData if there is no data in mongo" in new TestEnvironment {
      await(sessionStore.get()) should be(Right(None))
    }

    "return an error" when {

      "the data in mongo cannot be parsed" in new TestEnvironment {
        val invalidData                                                   = JsObject(Map("journeyStatus" -> JsNumber(1)))
        val create: Future[DatabaseUpdate[uk.gov.hmrc.cache.model.Cache]] =
          sessionStore.cacheRepository.createOrUpdate(
            Id(sessionId.value),
            sessionStore.sessionKey,
            invalidData
          )
        await(create).writeResult.inError shouldBe false
        await(sessionStore.get()).isLeft  shouldBe true
      }

      "there is no session id in the header carrier" in {
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val sessionData                = sample[SessionData]

        await(sessionStore.store(sessionData)).isLeft shouldBe true
        await(sessionStore.get()).isLeft              shouldBe true
      }

    }

  }

}

class SessionCacheFailureSpec extends AnyWordSpec with Matchers with MongoSupport {

  val sessionStore = new DefaultSessionCache(reactiveMongoComponent, config)

  reactiveMongoComponent.mongoConnector.helper.driver.close()

  val mongoIsBrokenAndAttemptingTo = new AfterWord(
    "mongo is broken and attempting to"
  )

  "SessionStore" must {

    "return an error" when mongoIsBrokenAndAttemptingTo {

      "insert a record" in new TestEnvironment {
        await(sessionStore.get()).isLeft shouldBe true
      }

      "read a record" in new TestEnvironment {
        val sessionData = sample[SessionData]

        await(sessionStore.store(sessionData)).isLeft shouldBe true
      }

    }

  }

}

object SessionCacheSpec {

  val config = Configuration(
    ConfigFactory.parseString(
      """
        | upscan-store.expiry-time = 2 days,
        | session-store.expiry-time = 7 days
        |""".stripMargin
    )
  )

  class TestEnvironment {

    val sessionId = SessionId(UUID.randomUUID().toString)

    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(sessionId))

  }

}
