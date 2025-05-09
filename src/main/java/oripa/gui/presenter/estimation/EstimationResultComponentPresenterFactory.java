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
package oripa.gui.presenter.estimation;

import java.util.function.Consumer;

import jakarta.inject.Inject;
import oripa.gui.presenter.estimation.logic.EstimationResultFilePresentationLogic;
import oripa.gui.view.estimation.EstimationResultUIView;

/**
 * @author OUCHI Koji
 *
 */
public class EstimationResultComponentPresenterFactory {

	private final EstimationResultFilePresentationLogic estimationResultFilePresentationLogic;

	@Inject
	public EstimationResultComponentPresenterFactory(
			final EstimationResultFilePresentationLogic estimationResultFilePresenter) {

		this.estimationResultFilePresentationLogic = estimationResultFilePresenter;
	}

	public EstimationResultUIPresenter createEstimationResultUIPresenter(
			final EstimationResultUIView view,
			final String lastFilePath,
			final Consumer<String> lastFilePathChangeListener) {
		return new EstimationResultUIPresenter(
				view,
				estimationResultFilePresentationLogic,
				lastFilePath,
				lastFilePathChangeListener);

	}
}
