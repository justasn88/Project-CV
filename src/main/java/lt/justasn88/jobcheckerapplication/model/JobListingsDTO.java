package lt.justasn88.jobcheckerapplication.model;

public record JobListingsDTO(String title, String url) {
    public JobListingsDTO {
        if (url != null && url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
    }
}

