package com.zy.alg.infoextra.multiinfo.lrentropy;

public class ArrayTool {

	public static int binarySearch(BaseNode[] branches, BaseNode node)
    {
        int high = branches.length - 1;
        if (branches.length < 1)
        {
            return high;
        }
        int low = 0;
        while (low <= high)
        {
            int mid = (low + high) >>> 1;
            int cmp = branches[mid].compareTo(node);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        return -(low + 1);
    }

	public static int binarySearch(BaseNode[] branches, char node)
    {
        int high = branches.length - 1;
        if (branches.length < 1)
        {
            return high;
        }
        int low = 0;
        while (low <= high)
        {
            int mid = (low + high) >>> 1;
            int cmp = branches[mid].compareTo(node);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        return -(low + 1);
    }
    
}
