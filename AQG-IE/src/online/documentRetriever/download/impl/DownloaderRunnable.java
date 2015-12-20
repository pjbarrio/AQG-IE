package online.documentRetriever.download.impl;

import java.io.Reader;
import java.net.URL;

import online.documentRetriever.download.Downloader;

import utils.thread.UnderTimeOutRunnable;

public class DownloaderRunnable<T> extends UnderTimeOutRunnable<String> {

	private URL url;
	private Downloader d;

	public DownloaderRunnable(Downloader d, URL url) {
		this.url = url;
		this.d = d;
	}

	@Override
	public String execute() {
				
		return d.retrieve(url);
				
	}

	

}
