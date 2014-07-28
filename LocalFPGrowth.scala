// Really dumb implementation of a tree, it is mutable, non-distributable :(, does not use zipper or scalaz
import collection.mutable

class Node(name:String, parent :Node = null, children: collection.mutable.Map[String,Node] = collection.mutable.Map(), var count:Int = 1) {

  def getName():String = return name

  def getCount():Int = return count

  def addChild(name:String):Node = {
    if (children contains name) {
      children(name).count += 1
    } else {
      children(name) = new Node(name, this);
    }
    return children(name)
  }

  def addChildren(names : Array[String]):Node = {
    if (names.isEmpty != true) {
      val child = addChild(names.head);
      child.addChildren(names.tail);
    }
    return this;
  }

  def getChildren():List[Node] = {
    children.map(_._2).toList
  }

  def getParent():Node = {
    return parent
  }

  def getChildrenAtAnyDepth(searchedNodeName:String):List[Node] = {
    getChildren.foldLeft(List():List[Node])((l,entry) => entry.getNodesWithName(searchedNodeName, l))
  }

  def getNodesWithName(searchedNodeName:String, results:List[Node]):List[Node] = {
    if (name.equals(searchedNodeName)) {
      return results :+ this
    } else {
      return results ++ getChildrenAtAnyDepth(searchedNodeName)
    }
  }
  override def toString():String = {
    return s"$name $children $count"
  }

  def supportValue(searchedNodeName:String):Int = {
    this.getChildrenAtAnyDepth(searchedNodeName).foldLeft(0:Int)((s,node)=> s + node.getCount)
  }

  def testValues():Node = {
    Array("b e a d","b e c", "b e a d","b e a c", "b e a c d", "b c d").foreach{ x:String => 
      val y = x.split(" ")
      this.addChildren(y)
    }
    return this;
  }

  def isNonRoot():Boolean = {
    return (!name.equals("root"))
  }
}

object FPGrowthOps {
  private def generatePathsTillNext(path:String, node:Node):List[String] = {
    if (node.isNonRoot) {
      val newPath = path + " " +node.getName
      return List(newPath.trim) ++ generatePathsTillNext(newPath, node.getParent)
    } else {
      return List()
    }
  }

  private def visit(visited:mutable.Set[Node], minSupport:Int, node: Node, searchedNodeName:String, path:String, paths:mutable.MutableList[String]):mutable.MutableList[String] = {
    if (node.isNonRoot && (!(visited contains node))) {
      val support = node.supportValue(searchedNodeName)
      visited + node
      if (support > minSupport) {
        val newPath = path + " " +node.getName
        paths += newPath.trim
        paths ++= generatePathsTillNext(newPath, node.getParent);
      } else {
        visit(visited, minSupport, node.getParent, searchedNodeName, path, paths)
      }
    }
    return paths
  }

  def projections(minSupport:Int, item:String, root:Node):List[String] = {
    val visited:mutable.Set[Node] = mutable.Set();
    var paths:mutable.MutableList[String] = mutable.MutableList();
    root.getChildrenAtAnyDepth(item).foreach{ node:Node =>
      visit(visited, minSupport, node.getParent, item, item, paths)
    }
    return List() ++ paths;
  }
}
