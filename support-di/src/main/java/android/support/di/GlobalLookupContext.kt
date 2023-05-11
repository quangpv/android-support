package android.support.di

class GlobalLookupContext(locator: BeanLocator) : LookupContext(locator),
    BeanLocator by locator