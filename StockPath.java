/**
 * Created by Juliet on 5/7/14.
 */
import java.util.*;
import javafx.util.Pair;
import org.joda.time.DateTime;

    // The interface for creating StockPath. The returned list should be ordered by date
    public interface StockPath {
        public ArrayList<Pair<DateTime,Double>> getPrices();
    }

