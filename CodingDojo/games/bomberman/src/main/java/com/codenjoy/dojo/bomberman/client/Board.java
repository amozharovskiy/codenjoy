package com.codenjoy.dojo.bomberman.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.bomberman.model.Elements;
import com.codenjoy.dojo.client.AbstractBoard;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;

import java.util.*;
import java.util.stream.Collectors;

import static com.codenjoy.dojo.bomberman.model.Elements.*;
import static com.codenjoy.dojo.services.Direction.*;
import static com.codenjoy.dojo.services.PointImpl.pt;

public class Board extends AbstractBoard<Elements> {

    public static final char ANY_CHAR = '?';

    @Override
    public Elements valueOf(char ch) {
        return Elements.valueOf(ch);
    }

    @Override
    protected int inversionY(int y) {
        return size - 1 - y;
    }

    @Override
    protected boolean withoutCorners() {
        return true;
    }

    public Elements getAt(int x, int y) {
        if (isOutOfField(x, y)) {
            return WALL;
        }
        return super.getAt(x, y);
    }

    public Collection<Point> getBarriers() {
        Collection<Point> all = getMeatChoppers();
        all.addAll(getWalls());
        all.addAll(getBombs());
        all.addAll(getDestroyableWalls());
        all.addAll(getOtherBombermans());

        return removeDuplicates(all);
    }

    @Override
    public String toString() {
        return String.format("%s\n" +
            "Bomberman at: %s\n" +
            "Other bombermans at: %s\n" +
            "Meat choppers at: %s\n" +
            "Destroy walls at: %s\n" +
            "Bombs at: %s\n" +
            "Blasts: %s\n" +
            "Expected blasts at: %s",
                boardAsString(),
                getBomberman(),
                getOtherBombermans(),
                getMeatChoppers(),
                getDestroyableWalls(),
                getBombs(),
                getBlasts(),
                getFutureBlasts());
    }

    public Point getBomberman() {
        return get(BOMBERMAN, BOMB_BOMBERMAN, DEAD_BOMBERMAN).get(0);
    }

    public Collection<Point> getOtherBombermans() {
        return get(OTHER_BOMBERMAN, OTHER_BOMB_BOMBERMAN, OTHER_DEAD_BOMBERMAN);
    }

    public boolean isMyBombermanDead() {
        return !get(DEAD_BOMBERMAN).isEmpty();
    }

    public Collection<Point> getMeatChoppers() {
        return get(MEAT_CHOPPER);
    }

    public Collection<Point> getWalls() {
        return get(WALL);
    }

    public Collection<Point> getDestroyableWalls() {
        return get(DESTROYABLE_WALL);
    }

    public Collection<Point> getBombs() {
        List<Point> result = new LinkedList<>();
        result.addAll(get(BOMB_TIMER_1));
        result.addAll(get(BOMB_TIMER_2));
        result.addAll(get(BOMB_TIMER_3));
        result.addAll(get(BOMB_TIMER_4));
        result.addAll(get(BOMB_TIMER_5));
        result.addAll(get(BOMB_BOMBERMAN));
        result.addAll(get(OTHER_BOMB_BOMBERMAN));
        return result;
    }

    public Collection<Point> getBlasts() {
        return get(BOOM);
    }

    public Collection<Point> getFutureBlasts() {        
        Collection<Point> bombs = getBombs();
        Collection<Point> result = new LinkedList<>();
        for (Point bomb : bombs) {
            result.add(bomb);
            // TODO remove duplicate (check same logic inside parrent isNear for example)
            result.add(pt(bomb.getX() - 1, bomb.getY()));
            result.add(pt(bomb.getX() + 1, bomb.getY()));
            result.add(pt(bomb.getX(), bomb.getY() - 1));
            result.add(pt(bomb.getX(), bomb.getY() + 1));
        }
        Collection<Point> result2 = new LinkedList<Point>();
        for (Point blast : result) {
            if (blast.isOutOf(size) || getWalls().contains(blast)) {
                continue;
            }
            result2.add(blast);
        }
        return removeDuplicates(result2);
    }

    public boolean isBarrierAt(int x, int y) {
        return getBarriers().contains(pt(x, y));
    }

    public boolean isBarrierAt(Point point) {
        return isBarrierAt(point.getX(), point.getY());
    }

    public Direction turnAnswer(Direction previousAct) {
        List<Point> freePoints = getAvailablePointsNear(getBomberman());
        Direction direction = getDirectionTo(freePoints.stream().findFirst().orElse(getBomberman()));

        return direction;
    }

    private Direction getDirectionTo(Point point) {
        Point bomber = getBomberman();
        if (bomber.getX() > point.getX()){
            return LEFT;
        }

        if (bomber.getX() < point.getX()){
            return RIGHT;
        }

        if (bomber.getY() > point.getY()){
            return UP;
        }

        if (bomber.getY() < point.getY()){
            return DOWN;
        }
        return STOP;
    }

    private List<Point> getAvailablePointsNear(Point point) {
        List<Point> points = Arrays.asList(new PointImpl(point.getX() + 1, point.getY()),
                new PointImpl(point.getX() - 1, point.getY()),
                new PointImpl(point.getX(), point.getY() + 1),
                new PointImpl(point.getX(), point.getY() - 1));
        return points.stream().filter(point1 -> isAt(point1, NONE)).collect(Collectors.toList());
    }
}
