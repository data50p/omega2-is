package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.anim.context.AnimContext;
import com.femtioprocent.omega.xml.Element;

import javax.swing.event.EventListenerList;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MasterTimeLine implements PlayCtrlListener {
    private PropertyChangeSupport pr_ch;
    private TimeLine[] timelines;
    private EventListenerList play_listeners;
    private EventListenerList edit_listeners;
    public AnimContext a_ctxt;
    private boolean dry;

    public MasterTimeLine(AnimContext a_ctxt) {
        this.a_ctxt = a_ctxt;

        pr_ch = new PropertyChangeSupport(this);
        timelines = new TimeLine[getMaxTimeLineIndex()];
        play_listeners = new EventListenerList();
        edit_listeners = new EventListenerList();
    }

    public void initNew() {
        timelines = new TimeLine[getMaxTimeLineIndex()];
    }

    public int getMaxDuration() {
        int max = 0;
        for (TimeLine tl : timelines) {
            if (tl != null) {
                int d = tl.getOffset() + tl.getDuration();
                if (d > max)
                    max = d;
            }
        }
        return max;
    }

    static public int getMaxTimeLineIndex() {
        return OmegaConfig.TIMELINES_N;
    }

    public TimeLine getTimeLine(int nid) {
        if (nid < 0 || nid >= timelines.length)
            return null;
        return timelines[nid];
    }

    public void addTimeLine(TimeLine tl) {
        timelines[tl.nid] = tl;
        pr_ch.firePropertyChange("timelines", null, timelines);
    }

    public void removeTimeLine(int nid) {
        TimeLine tl = timelines[nid];
        timelines[nid] = null;
    }

    public int getFreeTLIndex() {
        for (int i = 0; i < timelines.length; i++)
            if (timelines[i] == null) {
                return i;
            }
        return -1;
    }

    private List<TimeMarker> getMarkers(char type) {
        List<TimeMarker> l = new ArrayList<>();

        for (TimeLine tl : timelines) {
            if (tl == null)
                continue;
            l.addAll(tl.getMarkersType(type));
        }
        TimeMarker[] tla = (TimeMarker[]) l.toArray(new TimeMarker[0]);
        Arrays.sort(tla, TimeMarker.getComparator());
        return Arrays.asList(tla);
    }

    private List<TimeMarker> getMarkersAbs(int from, int to) {
        List<TimeMarker> l = new ArrayList<>();

        for (TimeLine tl : timelines) {
            if (tl == null)
                continue;
            l.addAll(tl.getMarkersAbs(from, to));
        }
        TimeMarker[] tla = l.toArray(new TimeMarker[0]);
        Arrays.sort(tla, TimeMarker.getComparator());
        return Arrays.asList(tla);
    }

//      private List getAllTimeLinesAt(int now) {
//  	List l = new ArrayList();
//  	for(int i = 0; i < timelines.length; i++) {
//  	    TimeLine tl = timelines[i];
//  	    if ( tl == null )
//  		;
//  	    l.add(tl);
//  	}
//  	return l;
//      }

    public void addPlayListener(PlayListener l) {
        play_listeners.add(PlayListener.class, l);
    }

    public void removePlayListener(PlayListener l) {
        play_listeners.remove(PlayListener.class, l);
    }

    public void addEditListener(EditListener l) {
        edit_listeners.add(EditListener.class, l);
    }

    public void removeEditListener(EditListener l) {
        edit_listeners.remove(EditListener.class, l);
    }

    public void fireEventMarkerAtTime(int from, int to) {
        List<TimeMarker> l = getMarkersAbs(from, to);
        Iterator<TimeMarker> it = l.iterator();
        while (it.hasNext()) {
            TimeMarker tm = it.next();

            Object[] lia = play_listeners.getListenerList();
            for (int i = 0; i < lia.length; i += 2) {
                ((PlayListener) lia[i + 1]).actionMarkerAtTime(tm, to, dry);
            }
        }
    }

    public void fireEventAtTime(int t) {
//	List l = getAllTimeLinesAt(t);
        TimeLine[] tlA = timelines; // (TimeLine[])l.toArray(new TimeLine[0]);

        Object[] lia = play_listeners.getListenerList();
        for (int i = 0; i < lia.length; i += 2) {
            ((PlayListener) lia[i + 1]).actionAtTime(tlA, t, 0, dry);
        }
    }

    public void updateEndMarkers(int when) {
        for (TimeLine tl : timelines) {
            if (tl == null)
                continue;
            tl.updateEndMarker(when);
        }
        stop_time = when;
    }

    public void updateBeginMarkers(int when) {
        for (TimeLine tl : timelines) {
            if (tl == null)
                continue;
            tl.updateBeginMarker(when);
        }
    }

    public void playBegin(boolean dry) {
        this.dry = dry;
//log	OmegaContext.sout_log.getLogger().info(":--: " + "PLAY BEGIN");
    }

    public void playEnd(int when) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "PLAY END");
        dry = false;
    }

    void playAt2(int last, int now) {
        fireEventMarkerAtTime(last, now);
        fireEventAtTime(now);
    }

    public void fillElement(Element el) {
        Element mel = new Element("MTL");

        for (TimeLine tl : timelines) {
            if (tl == null)
                continue;
            Element tel = tl.getElement();
            mel.add(tel);
        }

        el.add(mel);
    }

//      public void save(XML_PW xmlpw) {
//  	Element mel = new Element("MTL");
//  	xmlpw.push(mel);

//  	for(int i = 0; i < timelines.length; i++) {
//  	    TimeLine tl = timelines[i];
//  	    if ( tl == null )
//  		continue;
//  	    Element el = tl.getElement();
//  	    xmlpw.put(el);
//  	}

//  	xmlpw.pop();
//      }

    public void load(Element el) { // MTL
        timelines = new TimeLine[getMaxTimeLineIndex()];
        for (int i = 0; i < timelines.length; i++) {
            try {
                Element tel = el.findElement("TimeLine", i);
                if (tel == null)
                    break;
                TimeLine tl = new TimeLine(tel);
                updateEndMarkers(tl.getEndMarker());
                addTimeLine(tl);
            } catch (Exception ex) {
            }
        }
    }

    public String[] getLessonId_TimeLines() {
        ArrayList<String> li = new ArrayList<String>();

        for (TimeLine tl : timelines) {
            if (tl != null) {
                String lid = tl.getLessonId();
                if (lid != null && lid.length() > 0)
                    li.add(lid);
            }
        }

        return (String[]) li.toArray(new String[0]);
    }

    public int getNid(String lesson_id) {
        for (int i = 0; i < timelines.length; i++) {
            TimeLine tl = timelines[i];
            if (tl != null) {
                String lid = tl.getLessonId();
                if (lid != null)
                    if (lid.equals(lesson_id))
                        return i;
            }
        }
        return -1;
    }

    // --------- interface PlayCtrlListener

    int stop_time = 5000;
    int last_t;

    public void beginPlay(boolean dry) {
        last_t = 0;
        playBegin(dry);
    }

    public boolean playAt(int lt, int t) {
        playAt2(lt, t);
        return t >= stop_time;
    }

    public boolean playAt(int t) {
        playAt2(last_t, t);
        last_t = t;
        return t >= stop_time;
    }

    public void endPlay() {
        playEnd(last_t);
    }

    public void propertyChanged(String s) {
    }

    // --------

    public int getLastTimeTick() {
        return last_t;
    }

    public void setLastTimeTick(int t) {
        last_t = t;
    }

    public void setStopTime(int st) {
        stop_time = st;
    }

    public String getFirst2Path_LessonId(String verb) {
        try {
            String ss = "";
            int cnt = 0;
            for (int i = 0; i < 4; i++) {
                String s = getTimeLine(i).getLessonId();
                if (s.length() > 0) {
                    cnt++;
                    if (cnt == 1)
                        ss += "$" + s + " " + verb;
                    else {
                        return ss + " $" + s;
                    }
                }
            }
            return verb;
        } catch (NullPointerException ex) {
            return verb;
        }
    }

    public List<String> fetchPlaySound(boolean b[]) {
        List<String> li = new ArrayList<>();
        for (int i = 0; i < timelines.length; i++) {
            TimeLine tl = timelines[i];
            if (tl != null && b[i])
                tl.fetchPlaySound(li);
        }
        return li;
    }
} 
