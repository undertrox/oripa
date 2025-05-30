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
package oripa.renderer.estimation;

import java.util.Map;

import oripa.domain.fold.halfedge.OriFace;
import oripa.domain.fold.halfedge.OriHalfedge;
import oripa.domain.fold.halfedge.OriVertex;
import oripa.util.collection.CollectionUtil;

/**
 * @author OUCHI Koji
 *
 */
class FaceFactory {

	private final CoordinateConverter converter;
	private final Map<OriVertex, Integer> vertexDepths;

	/**
	 *
	 * @param converter
	 * @param vertexDepths
	 *            can be empty map if converter does not use.
	 */
	public FaceFactory(final CoordinateConverter converter, final Map<OriVertex, Integer> vertexDepths,
			final double eps) {
		this.converter = converter;
		this.vertexDepths = vertexDepths;
	}

	public Face create(final OriFace face, final double eps) {
		return new Face(face, convertCoordinate(face, eps));
	}

	private OriFace convertCoordinate(final OriFace face, final double eps) {
		var convertedFace = new OriFace();
		convertedFace.setFaceID(face.getFaceID());

		var convertedHalfedges = face.halfedgeStream()
				.map(OriHalfedge::getVertex)
				.map(v -> converter.convert(v.getPosition(), vertexDepths.get(v),
						v.getPositionBeforeFolding()))
				.map(p -> new OriHalfedge(new OriVertex(p), convertedFace))
				.toList();

		for (int i = 0; i < convertedHalfedges.size(); i++) {
			var he = convertedHalfedges.get(i);
			he.setNext(CollectionUtil.getCircular(convertedHalfedges, i + 1));
		}

		for (var he : convertedHalfedges) {
			convertedFace.addHalfedge(he);
		}

		if (!face.isFaceFront()) {
			convertedFace.invertFaceFront();
		}
		convertedFace.makeHalfedgeLoop(eps);

		return convertedFace;
	}

}
