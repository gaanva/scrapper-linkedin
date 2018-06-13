package com.rocasolida.scrap.util;

import lombok.Data;

@Data
public class Driver {
	private DriverType type;
	private String os;
	private String seleniumHost;
	private String seleniumPort;

	protected Driver(final DriverType type, final String os) {
		this.type = type;
		this.os = os;
	}

	protected Driver(final DriverType type, final String os, final String seleniumHost, final String seleniumPort) {
		this(type, os);
		this.seleniumHost = seleniumHost;
		this.seleniumPort = seleniumPort;
	}

	public static Driver from(final DriverType type, final String os) {
		return new Driver(type, os);
	}

	public static Driver from(final DriverType type, final String os, final String seleniumHost, final String seleniumPort) {
		return new Driver(type, os, seleniumHost, seleniumPort);
	}
}
