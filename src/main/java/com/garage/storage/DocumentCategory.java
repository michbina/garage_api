package com.garage.storage;

public enum DocumentCategory {

	DEVIS("devis"), FACTURES("factures"), BANNER("banners");

	private final String folder;

	DocumentCategory(String folder) {
		this.folder = folder;
	}

	public String getFolder() {
		return folder;
	}

}
