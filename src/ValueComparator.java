import java.util.Comparator;
import java.util.HashMap;

class ValueComparator implements Comparator<String> {
        HashMap<String, Double> map;
        ValueComparator(HashMap<String, Double> base) {
        this.map = base;
        }
       
		public int compare(String a, String b) {
            if (map.get(a) >= map.get(b)) {
                return -1;
            }
            else {
                return 1;
            }
            }
        }
