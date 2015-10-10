/*
 * Copyright (C) 2015 Marco
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.parking.negotiation;

import static java.lang.Math.log;

/**
 *
 * @author Marco
 */
public class ConcreteInputCalculator implements InputCalculator {

    private double maxPrice = 10.0;

    @Override
    public double getStaticPrice(int zone, int capacity) {

        return (maxPrice / (log(zone) + 1)) * (maxPrice / capacity);

    }

    @Override
    public double getDynamicPrice(int zone, int occupied, int capacity, boolean isEvent) {
        if (capacity - occupied <= 0) {
            return -1;
        }
        if (isEvent) {
            return (maxPrice / (log(zone) + 1)) * (maxPrice / (capacity - occupied)) + (1 + (int) (Math.random() * ((maxPrice - 1) + 1)));
        } else {
            return (maxPrice / (log(zone) + 1)) * (maxPrice / (capacity - occupied));
        }
    }
}
