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

object Records {
  def fromRows(header: List[String], data: List[Map[String, String]]): Records = ParseValues(header, data)

  val empty = Records(List.empty, List.empty)
  def emptyWithHeader(header: List[String]) = Records(header, List.empty)
}

case class Records(header: List[String], rows: List[Map[String, Any]]) {

  def equalsUnordered(otherRecords: Records): Boolean = {
    def equalHeaders = header == otherRecords.header
    def equalRows = rows.toSet == otherRecords.rows.toSet
    equalHeaders && equalRows
  }

  def equalsUnorderedWithUnorderedLists(otherRecords: Records): Boolean = {
    def equalHeaders = header == otherRecords.header
    def equalRows = rows.toSet == otherRecords.rows.toSet

    equalHeaders && equalRows
  }

}

/**
  * Mutable implementations implement .cypher
  * Immutable implementations implement .execute
  *
  * An implementation will not have to implement .cypher if .execute is overridden.
  */
trait Graph {
  def execute(query: String, params: Map[String, Any] = Map.empty): (Graph, Records) =
    this -> cypher(query, params)

  def cypher(query: String, params: Map[String, Any] = Map.empty): Records =
    throw new UnsupportedOperationException("To use the TCK, implement this method or override .execute()")
}
