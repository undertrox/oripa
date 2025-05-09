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
package oripa.domain.fold.condfac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oripa.domain.fold.halfedge.OriFace;
import oripa.domain.fold.halfedge.OriHalfedge;
import oripa.domain.fold.origeom.OverlapRelation;
import oripa.domain.fold.stackcond.StackConditionOf3Faces;
import oripa.util.StopWatch;

/**
 * @author OUCHI Koji
 *
 */
public class StackConditionOf3FaceFactory {
	private static final Logger logger = LoggerFactory.getLogger(StackConditionOf3FaceFactory.class);

	/**
	 * Creates 3-face condition: If face[i] and face[j] touching edge are
	 * covered by face[k] then OR[i][k] = OR[j][k]
	 *
	 * @param faces
	 *            all faces of the model
	 * @param overlapRelation
	 *            overlap relation matrix
	 */
	public List<StackConditionOf3Faces> createAll(
			final List<OriFace> faces, final OverlapRelation overlapRelation,
			final List<Integer>[][] overlappingFaceIndexIntersections,
			final Map<OriHalfedge, Set<Integer>> faceIndicesOnHalfedge) {

		var conditions = new ArrayList<StackConditionOf3Faces>();

		var watch = new StopWatch(true);

		for (OriFace f_i : faces) {
			var index_i = f_i.getFaceID();
			for (OriHalfedge he : f_i.halfedgeIterable()) {
				var pairOpt = he.getPair();

				pairOpt.map(OriHalfedge::getFace)
						.map(OriFace::getFaceID)
						.filter(index_j -> overlapRelation.isLower(index_i, index_j))
						.ifPresent(index_j -> conditions.addAll(createForIandJ(index_i, index_j,
								overlappingFaceIndexIntersections,
								faceIndicesOnHalfedge.get(he))));
			}
		}

		logger.debug("#condition3 = {}", conditions.size());
		logger.debug("condition3s computation time {}[ms]", watch.getMilliSec());

		return conditions;
	}

	private Collection<StackConditionOf3Faces> createForIandJ(
			final int index_i, final int index_j,
			final List<Integer>[][] overlappingFaceIndexIntersections,
			final Set<Integer> faceIndicesOnHalfedge) {

		var conditions = new ArrayList<StackConditionOf3Faces>();
		var indices = overlappingFaceIndexIntersections[index_i][index_j];
		for (var index_k : indices) {
			if (index_i == index_k || index_j == index_k) {
				continue;
			}
			if (!faceIndicesOnHalfedge.contains(index_k)) {
				continue;
			}

			var upper = index_i;
			var lower = index_j;
			var other = index_k;
			StackConditionOf3Faces cond = new StackConditionOf3Faces(lower, upper, other);

			conditions.add(cond);
		}

		return conditions;
	}

}
