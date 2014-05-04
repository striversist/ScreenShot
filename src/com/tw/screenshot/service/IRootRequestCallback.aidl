package com.tw.screenshot.service;

interface IRootRequestCallback {
	
	int commandOutput(int type, in String[] lines);
	
}