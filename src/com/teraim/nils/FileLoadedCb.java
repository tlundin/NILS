package com.teraim.nils;

public interface FileLoadedCb {

	enum ErrorCode {
		newVersionLoaded,
		notFound,
		parseError,
		ioError,
		sameold, 
		whatever, 
		configurationError
	}
	
	
	public void onFileLoaded(ErrorCode errCode);
	
}
