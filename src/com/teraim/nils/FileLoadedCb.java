package com.teraim.nils;

public interface FileLoadedCb {

	enum ErrorCode {
		newConfigVersionLoaded,
		newVarPatternVersionLoaded,
		tagLoaded,
		bothFilesLoaded,
		notFound,
		parseError,
		ioError,
		sameold, 
		whatever, 
		configurationError
	}
	
	
	public void onFileLoaded(ErrorCode errCode);
	
}
