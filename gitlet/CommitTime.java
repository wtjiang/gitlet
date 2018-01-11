package gitlet;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * @author Winston
 */

public class CommitTime {

    /** Formatted date. */
    private String formattedDate;

    /**
     * Commit Time constructor.
     * @param datee the date
     */
    public CommitTime(Date datee) {
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        formattedDate = dateFormat.format(datee);
    }

    /**
     * the time as a string.
     * @return time
     */
    public String getStringTime() {
        return this.formattedDate;
    }

}
