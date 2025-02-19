package com.github.talrey.createdeco.util;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface NonNullSupplier<T> {

	@Nonnull T get();
}
