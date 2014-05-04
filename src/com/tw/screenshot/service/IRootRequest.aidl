package com.tw.screenshot.service;

import com.tw.screenshot.service.IRootRequestCallback;

interface IRootRequest {
	
	int sendRequest(int type, in Bundle data, in IRootRequestCallback callback);
	
}