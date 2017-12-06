package org.opencypher.tools.tck

import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.opencypher.tools.tck.parsing.generated.{FeatureResultsBaseVisitor, FeatureResultsLexer, FeatureResultsParser}

import scala.collection.JavaConverters._
import scala.util.Try

case class CypherValueParseException(msg: String) extends Exception(msg)

object CypherValue {

  private val visitor = new CypherValueVisitor

  def apply(s: String): CypherValue = {
    val stream = CharStreams.fromString(s)
    val tokenStream = new FeatureResultsLexer(stream)
    val tokens = new CommonTokenStream(tokenStream)
    val parser = new FeatureResultsParser(tokens)
    val featureResultsContext = parser.value
    val value = visitor.visit(featureResultsContext)
    println(value)
    value
  }

}

sealed trait CypherValue

case class CypherNode(labels: Set[String], properties: CypherPropertyMap)
  extends CypherValue

case class CypherRelationship(relType: Option[String], properties: CypherPropertyMap) extends CypherValue

case class CypherString(s: String) extends CypherValue

case class CypherInteger(value: Long) extends CypherValue

case class CypherFloat(value: Double) extends CypherValue

case class CypherBoolean(value: Boolean) extends CypherValue

case class CypherProperty(key: String, value: CypherValue) extends CypherValue

case class CypherPropertyMap(properties: Map[String, CypherValue])
  extends CypherValue

case class CypherList(elements: List[CypherValue]) extends CypherValue

case object CypherNull extends CypherValue

case class CypherPath(connections: List[Connection]) extends CypherValue

case class Connection(s: CypherNode, r: CypherRelationship, t: CypherNode)

class CypherValueVisitor extends FeatureResultsBaseVisitor[CypherValue] {

  import FeatureResultsParser._

  override def visitValue(ctx: ValueContext): CypherValue = {
    Option(visitChildren(ctx)).getOrElse(CypherNull)
  }

  override def visitString(ctx: StringContext) = {
    val s = Try(ctx.STRING_LITERAL.getText).getOrElse("")
    CypherString(s)
  }

  override def visitNode(ctx: NodeContext) = {
    Try(visitNodeDesc(ctx.nodeDesc))
      .getOrElse(CypherNode(Set.empty, CypherPropertyMap(Map.empty)))
  }

  override def visitNodeDesc(ctx: NodeDescContext) = {
    val labels =
      Try(ctx.label.asScala.map(_.labelName.getText).toSet).getOrElse(Set.empty)
    val properties: CypherPropertyMap = Try(visitPropertyMap(ctx.propertyMap))
      .getOrElse(CypherPropertyMap(Map.empty))
    CypherNode(labels, properties)
  }

  override def visitRelationship(ctx: RelationshipContext) = {
    visitRelationshipDesc(ctx.relationshipDesc)
  }

  override def visitRelationshipDesc(ctx: RelationshipDescContext) = {
    val relType = Try(ctx.relationshipType.getText).toOption
    val properties: CypherPropertyMap =
      Try(visitPropertyMap(ctx.propertyMap))
        .getOrElse(CypherPropertyMap(Map.empty))
    CypherRelationship(relType, properties)
  }

  override def visitPath(ctx: PathContext) = {
    visitPathBody(ctx.pathBody)
  }

  override def visitPathBody(ctx: PathBodyContext) = {
    val initialNode = visitNodeDesc(ctx.nodeDesc)
    val (reversedPath, _) = ctx.pathLink.asScala.toList.foldLeft((List.empty[Connection], initialNode)) {
      case ((pathSoFar, currentNode), nextLink) =>
        val forward = Try(visitRelationshipDesc(nextLink.forwardsRelationship.relationshipDesc)).toOption
        val backward = Try(visitRelationshipDesc(nextLink.backwardsRelationship.relationshipDesc)).toOption
        val otherNode = visitNodeDesc(nextLink.nodeDesc)
        val updatedConnections = if (forward.isDefined) {
          Connection(currentNode, forward.get, otherNode) :: pathSoFar
        } else {
          Connection(currentNode, backward.get, otherNode) :: pathSoFar
        }
        (updatedConnections, otherNode)
    }
    CypherPath(reversedPath.reverse)
  }

  override def visitPathLink(ctx: PathLinkContext): CypherValue = ???

  override def visitForwardsRelationship(
                                          ctx: ForwardsRelationshipContext): CypherValue = ???

  override def visitBackwardsRelationship(
                                           ctx: BackwardsRelationshipContext): CypherValue = ???

  override def visitInteger(ctx: IntegerContext): CypherInteger = {
    CypherInteger(ctx.INTEGER_LITERAL.getText.toLong)
  }

  override def visitFloatingPoint(ctx: FloatingPointContext) = {
    val d = Option(ctx.INFINITY) match {
      case Some(i) =>
        if (i.getText.startsWith("-")) Double.NegativeInfinity
        else Double.PositiveInfinity
      case None => ctx.FLOAT_LITERAL.getText.toDouble
    }
    CypherFloat(d)
  }

  override def visitBool(ctx: BoolContext) = {
    CypherBoolean(ctx.getText.toBoolean)
  }

  override def visitNullValue(ctx: NullValueContext) = {
    CypherNull
  }

  override def visitList(ctx: ListContext): CypherValue = {
    val contents = Try(
      ctx.listContents.listElement.asScala
        .map(element => visitValue(element.value))
        .toList).toOption.getOrElse(List.empty)
    CypherList(contents)
  }

  override def visitMap(ctx: MapContext): CypherValue = {
    visitPropertyMap(ctx.propertyMap)
  }

  override def visitPropertyMap(ctx: PropertyMapContext) = {
    val keyValuePairs =
      Try(ctx.mapContents.keyValuePair.asScala.toList.map(visitKeyValuePair(_))).getOrElse(List.empty)
    CypherPropertyMap(
      keyValuePairs.map(kvPair => kvPair.key -> kvPair.value).toMap)
  }

  override def visitKeyValuePair(ctx: KeyValuePairContext) = {
    CypherProperty(ctx.propertyKey.getText,visitValue(ctx.propertyValue.value))
  }

}
