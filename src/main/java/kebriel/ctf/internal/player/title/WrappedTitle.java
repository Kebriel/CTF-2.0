package kebriel.ctf.internal.player.title;

import kebriel.ctf.internal.nms.GamePacket.DisplayTitle;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.CTFPlayer;

public class WrappedTitle {

    private final Text title;
    private final Text subtitle;
    private final int[] fades;

    public static WrappedTitle wrapBasic(Text title, Text subtitle, int[] fades) {
        return new WrappedTitle(title, subtitle, fades);
    }

    public static WrappedTitle forPlayerData(Text title, Text subtitle, int[] fades, CTFPlayer dataSource) {
        return new WrappedPlayerTitle(title, subtitle, fades, dataSource);
    }

    private WrappedTitle(Text title, Text subtitle, int[] fades) {
        this.title = title;
        this.subtitle = subtitle;
        this.fades = fades;
    }

    public DisplayTitle getDisplay() {
        if(title == null && subtitle == null)
            return null;

        return title != null && subtitle != null ? DisplayTitle.displayFullTitle(title.toString(), subtitle.toString(), fades[0], fades[1], fades[2])
                : title == null ? DisplayTitle.displayOnlySubtitle(subtitle.toString(), fades[0], fades[1], fades[2])
                : DisplayTitle.displayOnlyTitle(title.toString(), fades[0], fades[1], fades[2]);
    }

    public WrappedTitle titleFillNext(Object obj) {
        title.fillNext(obj);
        return this;
    }

    public WrappedTitle subtitleFillNext(Object obj) {
        subtitle.fillNext(obj);
        return this;
    }

    public boolean needsPlayerData() {
        return this instanceof WrappedPlayerTitle;
    }

    public static class WrappedPlayerTitle extends WrappedTitle {

        private CTFPlayer dataSource;

        private WrappedPlayerTitle(Text title, Text subtitle, int[] fades) {
            super(title, subtitle, fades);
        }

        private WrappedPlayerTitle(Text title, Text subtitle, int[] fades, CTFPlayer dataSource) {
            this(title, subtitle, fades);
            this.dataSource = dataSource;
        }

        @Override
        public DisplayTitle getDisplay() {
            if(super.title != null && dataSource != null)
                super.title.fillPlaceholders(dataSource);
            if(super.subtitle != null && dataSource != null)
                super.subtitle.fillPlaceholders(dataSource);

            return super.getDisplay();
        }

        public void supplyDatasource(CTFPlayer dataSource) {
            this.dataSource = dataSource;
        }

        public boolean hasDataSource() {
            return dataSource != null;
        }
    }
}
