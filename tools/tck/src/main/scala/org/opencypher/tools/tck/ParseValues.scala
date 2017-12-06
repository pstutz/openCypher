/*
 * Copyright (c) 2015-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
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
package org.opencypher.tools.tck

import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.opencypher.tools.tck.parsing.generated.{FeatureResultsLexer, FeatureResultsParser}

object ParseValues {

  def apply(header: List[String], data: List[Map[String, String]]): Records = {
    //println(s"header=$header data=$data")
    val parsed = data.map(row => row.mapValues(CypherValue(_)).view.force)
    //println(s"parsed = $parsed")
    //val cypherContext = parser.()

    //println(cypherContext.toStringTree(parser))

    //val visitor = CypherVisitor()
    //println(visitor.visit(cypherContext).pretty)

    Records.empty
  }

}
