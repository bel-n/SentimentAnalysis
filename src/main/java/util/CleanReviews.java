package util;

public class CleanReviews {

    public static String extractField(String rawReview, String fieldName) {
        try {
            String marker = "\\\"" + fieldName + "\\\": \\\"";
            int start = rawReview.indexOf(marker);
            if (start == -1) {
                marker = "\\\"" + fieldName + "\\\":\\\"";
                start = rawReview.indexOf(marker);
            }
            if (start == -1) return "N/A";
            start += marker.length();
            int end = rawReview.indexOf("\\\"", start);
            if (end == -1) return "N/A";
            return rawReview.substring(start, end).trim();
        } catch (Exception e) {
            return "N/A";
        }
    }

    public static String extractTopic(String rawReview) {
        try {
            int start = rawReview.indexOf("\"") + 1;
            int end = rawReview.indexOf("\"", start);
            return rawReview.substring(start, end);
        } catch (Exception e) {
            return "N/A";
        }
    }

    public static String extractReviewText(String rawReview) {
        try {
            String marker = "\\\"reviewText\\\": \\\"";
            int start = rawReview.indexOf(marker);
            if (start == -1) {
                marker = "\\\"reviewText\\\":\\\"";
                start = rawReview.indexOf(marker);
            }
            if (start == -1) return rawReview;

            start += marker.length();
            int end = rawReview.indexOf("\\\"", start);
            if (end == -1) return rawReview;

            return rawReview.substring(start, end)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", " ")
                    .trim();

        } catch (Exception e) {
            return rawReview;
        }
    }
}
