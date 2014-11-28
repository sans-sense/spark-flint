object DataSetManager {
  private val registry:collection.mutable.Map[String, Any] = collection.mutable.Map()

  def register(name:String, dataSet: Any) = {
    registry(name) = dataSet
  }

  def get(name:String):Any = {
    registry(name)
  }

  def unregister(name:String) = {
    registry.remove(name)
  }
}
