package lt.justasn88.jobcheckerapplication.service;

public interface ScraperDispatchService {
    void dispatchTask(String targetScraper, String host);

    void dispatchPaginationTask(String targetScraper, String host, int nextPage);
}