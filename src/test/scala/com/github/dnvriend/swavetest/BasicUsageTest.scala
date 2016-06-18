/*
 * Copyright 2016 Dennis Vriend
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

package com.github.dnvriend.swavetest

import com.github.dnvriend.TestSpec
import com.github.dnvriend.swavetest.BasicUsageTest.Foo
import swave.core._

import scala.concurrent.Future

object BasicUsageTest {

  case class Foo(s: String, i: Int, f: Double)

}

class BasicUsageTest extends TestSpec {

  it should "filter the sequence" in {
    val res: Future[String] =
      Stream(1, 2, 3)
        .filter(_ > 1)
        .map(_.toString)
        .to(Drain.head)
        .run()

    res.futureValue shouldBe "2"
  }

  it should "fold to a list" in {
    Stream.from(0).take(5).drainToList(5).futureValue shouldBe List(0, 1, 2, 3, 4)
  }

  it should "fold to a vector" in {
    Stream.from(0).take(5).drainToVector(5).futureValue shouldBe Vector(0, 1, 2, 3, 4)
  }

  it should "fan in to tuple" in {
    Stream.from(0)
      .attach(Stream.from(10))
      .attach(Stream.from(100))
      .fanInToTuple
      .take(2)
      .drainToList(10).futureValue shouldBe List((0, 10, 100), (1, 11, 101))
  }

  it should "fan in to product" in {
    Stream.from(0)
      .map(_.toString)
      .attach(Stream.from(10))
      .attach(Stream.from(100).map(_.toDouble))
      .fanInToProduct[Foo]
      .take(2)
      .drainToList(10).futureValue shouldBe List(Foo("0", 10, 100.0), Foo("1", 11, 101.0))
  }

  it should "group sequences" in {
    Stream.from(0)
      .grouped(5)
      .take(1)
      .fanOutBroadcast()
      .sub.map(_.toString).end
      .sub.end
      .fanInMerge()
      .drainToList(2).futureValue shouldBe List(List(0, 1, 2, 3, 4), "List(0, 1, 2, 3, 4)")
  }
}
