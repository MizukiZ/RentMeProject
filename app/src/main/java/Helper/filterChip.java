package Helper;

import com.plumillonforge.android.chipview.Chip;

public class filterChip implements Chip {

    private String mName;

    public filterChip(String name) {
        mName = name;
    }

    @Override
    public String getText() {
        return mName;
    }


}
