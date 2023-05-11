package android.support.di

class ScopeLookupContext(
    private val context: GlobalLookupContext,
    override val owner: ScopeOwner,
) : LookupContext(context), ScopeLookup {

    private fun newContext(newOwner: ScopeOwner): LookupContext {
        return ScopeLookupContext(context, newOwner)
    }

    fun globalContext() = context

    fun getOrCreate(newOwner: ScopeOwner): LookupContext {
        return if (newOwner == owner) this
        else newContext(newOwner)
    }
}