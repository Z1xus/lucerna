package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.state.ValueUpdateNotifier;

public abstract class Uniform {
	protected final int location;
	protected final ValueUpdateNotifier notifier;

	protected Uniform(int location) {
		this(location, null);
	}

	protected Uniform(int location, ValueUpdateNotifier notifier) {
		this.location = location;
		this.notifier = notifier;
	}

	public abstract void update();

	public final int getLocation() {
		return location;
	}

	public final ValueUpdateNotifier getNotifier() {
		return notifier;
	}
}
