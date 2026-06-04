open class OpenClassRemovedTAImpl(x: Int) : Foo()
data class OpenClassRemovedTATypeParameterHolder<T : Foo>(val t: T)
data class OpenClassRemovedTAImplTypeParameterHolder<T : OpenClassRemovedTAImpl>(val t: T)

fun getOpenClassRemovedTA(x: Int): Foo = Foo()
fun setOpenClassRemovedTA(value: Foo?): String = value?.toString() ?: "setOpenClassRemovedTA"
fun getOpenClassRemovedTAImpl(x: Int): Foo = OpenClassRemovedTAImpl(x)
fun setOpenClassRemovedTAImpl(value: OpenClassRemovedTAImpl?): String = value?.toString() ?: "setOpenClassRemovedTAImpl"

fun getOpenClassRemovedTATypeParameterHolder1(x: Int): OpenClassRemovedTATypeParameterHolder<Foo> = OpenClassRemovedTATypeParameterHolder(Foo())
fun getOpenClassRemovedTATypeParameterHolder2(x: Int): OpenClassRemovedTATypeParameterHolder<OpenClassRemovedTAImpl> = OpenClassRemovedTATypeParameterHolder(OpenClassRemovedTAImpl(x))
fun setOpenClassRemovedTATypeParameterHolder1(value: OpenClassRemovedTATypeParameterHolder<Foo>?): String = value?.toString() ?: "setOpenClassRemovedTATypeParameterHolder1"
fun setOpenClassRemovedTATypeParameterHolder2(value: OpenClassRemovedTATypeParameterHolder<OpenClassRemovedTAImpl>?): String = value?.toString() ?: "setOpenClassRemovedTATypeParameterHolder2"

fun getOpenClassRemovedTAImplTypeParameterHolder(x: Int): OpenClassRemovedTAImplTypeParameterHolder<OpenClassRemovedTAImpl> = OpenClassRemovedTAImplTypeParameterHolder(OpenClassRemovedTAImpl(x))
fun setOpenClassRemovedTAImplTypeParameterHolder(value: OpenClassRemovedTAImplTypeParameterHolder<OpenClassRemovedTAImpl>?): String = value?.toString() ?: "setOpenClassRemovedTAImplTypeParameterHolder"
