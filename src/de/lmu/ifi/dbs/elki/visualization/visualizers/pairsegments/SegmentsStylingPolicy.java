package de.lmu.ifi.dbs.elki.visualization.visualizers.pairsegments;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2012
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.ids.ModifiableDBIDs;
import de.lmu.ifi.dbs.elki.evaluation.clustering.pairsegments.Segment;
import de.lmu.ifi.dbs.elki.evaluation.clustering.pairsegments.Segments;
import de.lmu.ifi.dbs.elki.visualization.colors.ColorLibrary;
import de.lmu.ifi.dbs.elki.visualization.style.ClassStylingPolicy;
import de.lmu.ifi.dbs.elki.visualization.style.StyleLibrary;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGUtil;

/**
 * Styling policy to communicate the segment selection to other visualizers.
 * 
 * @author Sascha Goldhofer
 * @author Erich Schubert
 */
public class SegmentsStylingPolicy implements ClassStylingPolicy {
  /**
   * The segments we use for visualization
   */
  protected final Segments segments;

  /**
   * Selected segments
   */
  protected ArrayList<Segment> selectedSegments = new ArrayList<Segment>();

  /**
   * Segments indirectly selected
   */
  protected TreeMap<Segment, Segment> indirectSelections = new TreeMap<Segment, Segment>();

  /**
   * Not selected IDs that will be drawn in default colors.
   */
  protected ModifiableDBIDs unselectedObjects = DBIDUtil.newHashSet();

  /**
   * Color library (only used in compatibility mode)
   */
  // TODO: move to abstract super class?
  ColorLibrary colorset;

  /**
   * Constructor.
   * 
   * @param segments Segments
   */
  public SegmentsStylingPolicy(Segments segments, StyleLibrary style) {
    super();
    this.segments = segments;
    this.colorset = style.getColorSet(StyleLibrary.PLOT);

    // get all selectable segments
    for(Segment segment : segments) {
      // store segmentID
      if(!segment.isUnpaired()) {
        // and store their get all objects
        if(segment.getDBIDs() != null) {
          unselectedObjects.addDBIDs(segment.getDBIDs());
        }
      }
    }
  }

  /**
   * Test whether a segment is selected.
   * 
   * @param segment Segment to test
   * @return true when selected
   */
  public boolean isSelected(Segment segment) {
    return selectedSegments.contains(segment) || indirectSelections.containsValue(segment);
  }

  @Override
  public int getStyleForDBID(DBID id) {
    Iterator<Segment> s = selectedSegments.iterator();
    for(int i = 0; s.hasNext(); i++) {
      Segment seg = s.next();
      DBIDs ids = seg.getDBIDs();
      if(ids != null && ids.contains(id)) {
        return i;
      }
    }
    return -2;
  }

  @Override
  public int getColorForDBID(DBID id) {
    int style = getStyleForDBID(id);
    // FIXME: slow
    return SVGUtil.stringToColor(colorset.getColor(style)).getRGB();
  }

  @Override
  // -2=grau, -1=schwarz, 0+=farben
  public int getMinStyle() {
    return -2;
  }

  @Override
  public int getMaxStyle() {
    return selectedSegments.size();
  }

  @Override
  public Iterator<DBID> iterateClass(int cnum) {
    // unselected
    if(cnum == -2) {
      return unselectedObjects.iterator();
    }
    else if(cnum == -1) {
      return DBIDUtil.EMPTYDBIDS.iterator();
    }
    // colors
    DBIDs ids = selectedSegments.get(cnum).getDBIDs();
    return (ids != null) ? ids.iterator() : DBIDUtil.EMPTYDBIDS.iterator();
  }

  /**
   * Adds or removes the given segment to the selection. Depending on the
   * clustering and cluster selected and the addToSelection option given, the
   * current selection will be modified. This method is called by clicking on a
   * segment and ring and the CTRL-button status.
   * 
   * Adding selections does only work on the same clustering and cluster, else a
   * new selection will be added.
   * 
   * @param segment the selected element representing a segment ring (specific
   *        clustering)
   * @param addToSelection flag for adding segment to current selection
   */
  public void select(Segment segment, boolean addToSelection) {
    // abort if segment represents pairs inNone. Would select all segments...
    if(segment.isNone()) {
      return;
    }
    if(!addToSelection) {
      deselectAllSegments();
    }

    // get selected segments
    if(segment.isUnpaired()) {
      // check if all segments are selected
      if(addToSelection) {
        boolean allSegmentsSelected = true;
        for(Segment other : segments.getPairedSegments(segment)) {
          if(!isSelected(other)) {
            allSegmentsSelected = false;
            break;
          }
        }

        // if all are selected, deselect all
        if(allSegmentsSelected) {
          deselectSegment(segment);
          return;
        }
      }
      if(isSelected(segment)) {
        deselectSegment(segment);
      }
      else {
        selectSegment(segment);
      }
    }
    else {
      // an object segment was selected
      if(isSelected(segment)) {
        deselectSegment(segment);
      }
      else {
        selectSegment(segment);
      }
    }
  }

  /**
   * Deselect all currently selected segments
   */
  public void deselectAllSegments() {
    while(selectedSegments.size() > 0) {
      deselectSegment(selectedSegments.get(selectedSegments.size() - 1));
    }
  }

  /**
   * Deselect a segment
   * 
   * @param segment Segment to deselect
   */
  protected void deselectSegment(Segment segment) {
    if(segment.isUnpaired()) {
      ArrayList<Segment> remove = new ArrayList<Segment>();
      // remove all object segments associated with unpaired segment from
      // selection list
      for(Entry<Segment, Segment> entry : indirectSelections.entrySet()) {
        if(entry.getValue() == segment) {
          remove.add(entry.getKey());
        }
      }

      for(Segment other : remove) {
        indirectSelections.remove(other);
        deselectSegment(other);
      }
    }
    else {
      // check if deselected object Segment has a unpaired segment highlighted
      Segment unpaired = indirectSelections.get(segment);
      if(unpaired != null) {
        // remove highlight
        deselectSegment(unpaired);
      }
      if(selectedSegments.remove(segment)) {
        if(segment.getDBIDs() != null) {
          unselectedObjects.addDBIDs(segment.getDBIDs());
        }
      }
    }
  }

  /**
   * Select a segment
   * 
   * @param segment Segment to select
   */
  protected void selectSegment(Segment segment) {
    if(segment.isUnpaired()) {
      // remember selected unpaired segment
      for(Segment other : segments.getPairedSegments(segment)) {
        indirectSelections.put(other, segment);
        selectSegment(other);
      }
    }
    else {
      if(!selectedSegments.contains(segment)) {
        selectedSegments.add(segment);
        if(segment.getDBIDs() != null) {
          unselectedObjects.removeDBIDs(segment.getDBIDs());
        }
      }
    }
  }

  /**
   * Get the index of a selected segment.
   * 
   * @param segment Segment to find
   * @return Index, or -1
   */
  public int indexOfSegment(Segment segment) {
    return selectedSegments.indexOf(segment);
  }
}