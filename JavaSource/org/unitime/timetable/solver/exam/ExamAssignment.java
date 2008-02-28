package org.unitime.timetable.solver.exam;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.LocationDAO;

import net.sf.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamRoom;

public class ExamAssignment extends ExamInfo implements Serializable, Comparable {
    protected Long iPeriodId = null;
    protected Vector iRoomIds = null;
    protected String iPeriodPref = null;
    protected int iPeriodIdx = -1;
    protected Hashtable iRoomPrefs = null;
    protected transient ExamPeriod iPeriod = null;
    protected transient TreeSet iRooms = null;
    protected ExamInfo iExam = null;
    
    public ExamAssignment(ExamPlacement placement) {
        super((net.sf.cpsolver.exam.model.Exam)placement.variable());
        iPeriodId = placement.getPeriod().getId();
        iPeriodIdx = placement.getPeriod().getIndex();
        iRoomIds = new Vector(placement.getRooms()==null?0:placement.getRooms().size());
        iPeriodPref = PreferenceLevel.int2prolog(placement.getPeriodPenalty());
        iRoomPrefs = new Hashtable();
        if (placement.getRooms()!=null)
            for (Iterator i=placement.getRooms().iterator();i.hasNext();) {
                ExamRoom room = (ExamRoom)i.next();
                iRoomIds.add(room.getId());
                iRoomPrefs.put(room.getId(),PreferenceLevel.int2prolog(((net.sf.cpsolver.exam.model.Exam)placement.variable()).getWeight(room)));
            }
    }
    
    public ExamAssignment(Exam exam) {
        super(exam);
        iPeriod = exam.getAssignedPeriod();
        iPeriodId = exam.getAssignedPeriod().getUniqueId();
        iRooms = new TreeSet();
        iRoomIds = new Vector(exam.getAssignedRooms().size());
        for (Iterator i=exam.getAssignedRooms().iterator();i.hasNext();) {
            Location location = (Location)i.next();
            iRooms.add(location);
            iRoomIds.add(location.getUniqueId());
        }
    }

    public Long getPeriodId() {
        return iPeriodId;
    }
    
    public ExamPeriod getPeriod() {
        if (iPeriod==null)
            iPeriod = new ExamPeriodDAO().get(getPeriodId());
        return iPeriod;
    }
    
    public Comparable getPeriodOrd() {
        if (iPeriodIdx>=0) return new Integer(iPeriodIdx);
        else return iPeriod;
    }

    public String getPeriodName() {
        ExamPeriod period = getPeriod();
        return period==null?"":period.getName();
    }
    
    public String getPeriodAbbreviation() {
        ExamPeriod period = getPeriod();
        return period==null?"":period.getAbbreviation();
    }

    public String getPeriodNameWithPref() {
        if (iPeriodPref==null || PreferenceLevel.sNeutral.equals(iPeriodPref)) return getPeriodName();
        return
            "<span title='"+PreferenceLevel.prolog2string(iPeriodPref)+" "+getPeriodName()+"' style='color:"+PreferenceLevel.prolog2color(iPeriodPref)+";'>"+
            getPeriodName()+
            "</span>";
    }
    
    public String getPeriodAbbreviationWithPref() {
        if (iPeriodPref==null || PreferenceLevel.sNeutral.equals(iPeriodPref)) return getPeriodAbbreviation();
        return
            "<span title='"+PreferenceLevel.prolog2string(iPeriodPref)+" "+getPeriodName()+"' style='color:"+PreferenceLevel.prolog2color(iPeriodPref)+";'>"+
            getPeriodAbbreviation()+
            "</span>";
    }

    public Vector getRoomIds() {
        return iRoomIds;
    }
    
    public TreeSet getRooms() {
        if (iRooms==null) {
            iRooms = new TreeSet();
            for (Enumeration e=getRoomIds().elements();e.hasMoreElements();) {
                Location location = new LocationDAO().get((Long)e.nextElement());
                iRooms.add(location);
            }
        }
        return iRooms;
    }
    
    public String getRoomsName(String delim) {
        String rooms = "";
        for (Iterator j=getRooms().iterator();j.hasNext();) {
            Location location = (Location)j.next();
            if (rooms.length()>0) rooms+=delim;
            rooms += location.getLabel();
        }
        return rooms;
    }

    public String getRoomsNameWithPref(String delim) {
        String rooms = "";
        for (Iterator j=getRooms().iterator();j.hasNext();) {
            Location location = (Location)j.next();
            if (rooms.length()>0) rooms+=delim;
            String roomPref = (iRoomPrefs==null?null:(String)iRoomPrefs.get(location.getUniqueId()));
            if (roomPref==null) {
                rooms += location.getLabel();
            } else {
                rooms += "<span title='"+PreferenceLevel.prolog2string(roomPref)+" "+location.getLabel()+"' style='color:"+PreferenceLevel.prolog2color(roomPref)+";'>"+
                location.getLabel()+
                "</span>";
            }
        }
        return rooms;
    }
    
    public String toString() {
        return getExamName()+" "+getPeriodAbbreviation()+" "+getRoomsName(",");
    }
    
    public String getPeriodPref() {
        return iPeriodPref;
    }
    
    public String getRoomPref(Long roomId) {
        if (iRoomPrefs==null) return null;
        return (String)iRoomPrefs.get(roomId);
    }

    public String getRoomPref() {
        if (iRoomPrefs==null) return null;
        MinMaxPreferenceCombination c = new MinMaxPreferenceCombination();
        for (Enumeration e=iRoomPrefs.elements();e.hasMoreElements();) {
            c.addPreferenceProlog((String)e.nextElement());
        }
        return c.getPreferenceProlog();
    }
}
