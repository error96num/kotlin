import abitestutils.abiTest

fun box() = abiTest {
    expectFailure(
        linkage("Function 'getOpenClassRemovedTA' can not be called: Function uses unlinked class symbol '/Foo'")
    ) { getOpenClassRemovedTA(1).toString() }

    expectFailure(
        linkage("Function 'setOpenClassRemovedTA' can not be called: Function uses unlinked class symbol '/Foo'")
    ) { setOpenClassRemovedTA(null) }

    expectFailure(
        linkage("Function 'getOpenClassRemovedTAImpl' can not be called: Function uses unlinked class symbol '/Foo'")
    ) { getOpenClassRemovedTAImpl(2).toString() }

    expectFailure(
        linkage("Function 'setOpenClassRemovedTAImpl' can not be called: Function uses unlinked class symbol '/Foo' (via class 'OpenClassRemovedTAImpl')")
    ) { setOpenClassRemovedTAImpl(null) }

    expectFailure(
        linkage("Function 'getOpenClassRemovedTATypeParameterHolder1' can not be called: Function uses unlinked class symbol '/Foo' (via data class 'OpenClassRemovedTATypeParameterHolder')")
    ) { getOpenClassRemovedTATypeParameterHolder1(3).toString() }

    expectFailure(
        linkage("Function 'getOpenClassRemovedTATypeParameterHolder2' can not be called: Function uses unlinked class symbol '/Foo' (via data class 'OpenClassRemovedTATypeParameterHolder')")
    ) { getOpenClassRemovedTATypeParameterHolder2(4).toString() }

    expectFailure(
        linkage("Function 'setOpenClassRemovedTATypeParameterHolder1' can not be called: Function uses unlinked class symbol '/Foo' (via data class 'OpenClassRemovedTATypeParameterHolder')")
    ) { setOpenClassRemovedTATypeParameterHolder1(null) }

    expectFailure(
        linkage("Function 'setOpenClassRemovedTATypeParameterHolder2' can not be called: Function uses unlinked class symbol '/Foo' (via data class 'OpenClassRemovedTATypeParameterHolder')")
    ) { setOpenClassRemovedTATypeParameterHolder2(null) }

    expectFailure(
        linkage("Function 'getOpenClassRemovedTAImplTypeParameterHolder' can not be called: Function uses unlinked class symbol '/Foo' (via data class 'OpenClassRemovedTAImplTypeParameterHolder')")
    ) { getOpenClassRemovedTAImplTypeParameterHolder(5).toString() }

    expectFailure(
        linkage("Function 'setOpenClassRemovedTAImplTypeParameterHolder' can not be called: Function uses unlinked class symbol '/Foo' (via data class 'OpenClassRemovedTAImplTypeParameterHolder')")
    ) { setOpenClassRemovedTAImplTypeParameterHolder(null) }
}
