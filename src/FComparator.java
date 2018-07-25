import java.util.Comparator;
//Determines where to place a searchnode in priority queue based off their f value
public class FComparator implements Comparator<SearchNode>{
    @Override
    public int compare(SearchNode x, SearchNode y) {
        return x.getfCost()-y.getfCost();
    }
}

