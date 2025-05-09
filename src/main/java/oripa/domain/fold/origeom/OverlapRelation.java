/**
 * ORIPA - Origami Pattern Editor
 * Copyright (C) 2013-     ORIPA OSS Project  https://github.com/oripa/oripa
 * Copyright (C) 2005-2009 Jun Mitani         http://mitani.cs.tsukuba.ac.jp/

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package oripa.domain.fold.origeom;

import oripa.util.AtomicByteDenseMatrix;
import oripa.util.BitBlockByteMatrix;
import oripa.util.ByteMatrix;
import oripa.util.ByteSparseMatrix;

/**
 * A wrapper of integer matrix for overlap relation operations.
 *
 * @author OUCHI Koji
 *
 */
public class OverlapRelation {
	private ByteMatrix overlapRelation;

	private static final byte NO_OVERLAP = 0;
	private static final byte UPPER = 1;
	private static final byte LOWER = 2;
	private static final byte UNDEFINED = 3;

	private byte toInternal(final byte value) {
		return switch (value) {
		case OverlapRelationValues.NO_OVERLAP -> NO_OVERLAP;
		case OverlapRelationValues.UPPER -> UPPER;
		case OverlapRelationValues.LOWER -> LOWER;
		case OverlapRelationValues.UNDEFINED -> UNDEFINED;
		default -> throw new IllegalArgumentException("Unexpected value: " + value);
		};
	}

	private byte toExternal(final byte value) {
		return switch (value) {
		case NO_OVERLAP -> OverlapRelationValues.NO_OVERLAP;
		case UPPER -> OverlapRelationValues.UPPER;
		case LOWER -> OverlapRelationValues.LOWER;
		case UNDEFINED -> OverlapRelationValues.UNDEFINED;
		default -> throw new IllegalArgumentException("Unexpected value: " + value);
		};

	}

	/**
	 * Internally creates a n x n matrix where n is the given {@code faceCount}.
	 *
	 * @param faceCount
	 *            the number of faces of the model.
	 */
	public OverlapRelation(final int faceCount) {
		overlapRelation = new BitBlockByteMatrix(faceCount, faceCount, 2);
//		overlapRelation = new ByteDenseMatrix(faceCount, faceCount);
	}

	private OverlapRelation(final ByteMatrix mat) {
		overlapRelation = mat.clone();
	}

	private OverlapRelation() {

	}

	/**
	 * @return deep copy of this instance.
	 */
	@Override
	public OverlapRelation clone() {
		return new OverlapRelation(overlapRelation);
	}

	/**
	 * Returns clone for parallel computing. Incomplete implementation so far:
	 * returned object doesn't have atomicity for setXXX().
	 *
	 * @return
	 */
	@Deprecated
	public OverlapRelation cloneAtomic() {
		var cloned = new OverlapRelation();

		cloned.overlapRelation = new AtomicByteDenseMatrix(getSize(), getSize());

		copyTo(cloned);

		return cloned;
	}

	public void copyTo(final OverlapRelation o) {
		for (int i = 0; i < getSize(); i++) {
			for (int j = i; j < getSize(); j++) {
				o.set(i, j, get(i, j));
			}
		}
	}

	public void switchToSparseMatrix() {
		var sparse = new ByteSparseMatrix(overlapRelation.rowCount(), overlapRelation.columnCount());
		for (int i = 0; i < overlapRelation.rowCount(); i++) {
			for (int j = 0; j < overlapRelation.columnCount(); j++) {
				sparse.set(i, j, overlapRelation.get(i, j));
			}
		}
		overlapRelation = sparse;
	}

	/**
	 *
	 * @param i
	 *            row index
	 * @param j
	 *            column index
	 * @return [i][j] value.
	 */
	public byte get(final int i, final int j) {
		return toExternal(overlapRelation.get(i, j));
	}

	/**
	 * @return the n of n x n matrix.
	 */
	public int getSize() {
		return overlapRelation.rowCount();
	}

	/**
	 * Sets {@code value} to {@code overlapRelation[i][j]}. This method sets
	 * inversion of {@code value} to {@code overlapRelation[j][i]}.
	 *
	 * @param i
	 *            row index
	 * @param j
	 *            column index
	 * @param value
	 *            a value of {@link OverlapRelationValues}
	 * @throws IllegalArgumentException
	 *             when {@code value} is not of {@link OverlapRelationValues}.
	 */
	public void set(final int i, final int j, final byte value) throws IllegalArgumentException {
		var internalValue = toInternal(value);
		overlapRelation.set(i, j, internalValue);

		switch (internalValue) {
		case LOWER:
			overlapRelation.set(j, i, UPPER);
			break;
		case UPPER:
			overlapRelation.set(j, i, LOWER);
			break;
		case UNDEFINED, NO_OVERLAP:
			overlapRelation.set(j, i, internalValue);
			break;

		default:
			throw new IllegalArgumentException("value argument is wrong.");
		}
	}

	/**
	 * Sets {@link OverlapRelationValues#LOWER} to
	 * {@code overlapRelation[i][j]}. This method sets
	 * {@link OverlapRelationValues#UPPER} to {@code overlapRelation[j][i]}.
	 *
	 * @param i
	 *            row index
	 * @param j
	 *            column index
	 */
	public void setLower(final int i, final int j) {
		set(i, j, OverlapRelationValues.LOWER);
	}

	/**
	 * Sets {@link OverlapRelationValues#UPPER} to
	 * {@code overlapRelation[i][j]}. This method sets
	 * {@link OverlapRelationValues#LOWER} to {@code overlapRelation[j][i]}.
	 *
	 * @param i
	 *            row index
	 * @param j
	 *            column index
	 */
	public void setUpper(final int i, final int j) {
		set(i, j, OverlapRelationValues.UPPER);
	}

	/**
	 * Sets {@link OverlapRelationValues#UNDEFINED} to
	 * {@code overlapRelation[i][j]} and {@code overlapRelation[j][i]}.
	 *
	 * @param i
	 *            row index
	 * @param j
	 *            column index
	 */
	public void setUndefined(final int i, final int j) {
		set(i, j, OverlapRelationValues.UNDEFINED);
	}

	/**
	 * Sets {@link OverlapRelationValues#NO_OVERLAP} to
	 * {@code overlapRelation[i][j]} and {@code overlapRelation[j][i]}.
	 *
	 * @param i
	 *            row index
	 * @param j
	 *            column index
	 */
	public void setNoOverlap(final int i, final int j) {
		set(i, j, OverlapRelationValues.NO_OVERLAP);
	}

	/**
	 *
	 * @return true if LOWER and UPPER are set to [i][j] and [j][i]
	 *         respectively.
	 */
	public boolean setLowerIfUndefined(final int i, final int j) {
		return setIfUndefined(i, j, OverlapRelationValues.LOWER);
	}

	public boolean setUpperIfUndefined(final int i, final int j) {
		return setIfUndefined(i, j, OverlapRelationValues.UPPER);
	}

	public boolean setIfUndefined(final int i, final int j, final byte value) {
		if (!isUndefined(i, j)) {
			return false;
		}

		set(i, j, value);
		return true;
	}

	/**
	 *
	 * @return {@code true} if {@code overlapRelation[i][j]} is equal to
	 *         {@link OverlapRelationValues#LOWER}.
	 */
	public boolean isLower(final int i, final int j) {
		return overlapRelation.get(i, j) == LOWER;
	}

	/**
	 *
	 * @return {@code true} if {@code overlapRelation[i][j]} is equal to
	 *         {@link OverlapRelationValues#UPPER}.
	 */
	public boolean isUpper(final int i, final int j) {
		return overlapRelation.get(i, j) == UPPER;
	}

	/**
	 *
	 * @return {@code true} if {@code overlapRelation[i][j]} is equal to
	 *         {@link OverlapRelationValues#UNDEFINED}.
	 */
	public boolean isUndefined(final int i, final int j) {
		return overlapRelation.get(i, j) == UNDEFINED;
	}

	/**
	 *
	 * @return {@code true} if {@code overlapRelation[i][j]} is equal to
	 *         {@link OverlapRelationValues#NO_OVERLAP}.
	 */
	public boolean isNoOverlap(final int i, final int j) {
		return overlapRelation.get(i, j) == NO_OVERLAP;
	}

	public EstimationResult setLowerIfPossible(final int i, final int j) {

		return setIfPossible(i, j, OverlapRelationValues.LOWER);
	}

	public EstimationResult setUpperIfPossible(final int i, final int j) {
		return setIfPossible(i, j, OverlapRelationValues.UPPER);
	}

	public EstimationResult setIfPossible(final int i, final int j, final byte value) {

		if (setIfUndefined(i, j, value)) {
			return EstimationResult.CHANGED;
		}
		if (get(i, j) != value) {
			// conflict.
			return EstimationResult.UNFOLDABLE;
		}

		return EstimationResult.NOT_CHANGED;
	}

	@Override
	public String toString() {
		var builder = new StringBuilder();

		for (int i = 0; i < getSize(); i++) {
			var line = String.join(" ",
					overlapRelation.getRow(i).stream()
							.map(b -> b.toString())
							.toList());
			builder.append(line);
			builder.append(System.lineSeparator());
		}

		return builder.toString();
	}

}
