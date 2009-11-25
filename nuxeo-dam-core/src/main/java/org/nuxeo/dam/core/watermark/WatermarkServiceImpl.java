package org.nuxeo.dam.core.watermark;

import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_HEIGHT;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_WIDTH;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.dam.api.WatermarkService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.picture.api.ImagingService;
import org.nuxeo.runtime.api.Framework;

public class WatermarkServiceImpl implements WatermarkService {
	
	private final Logger log = Logger.getLogger(WatermarkServiceImpl.class);

	private File defaultWatermarkFile;

	private ImagingService imagingService;

	public File performWatermarkOnFile(File inputFilePath) throws Exception {

		Map<String, Object> imageMetadata = getImageMetadata(inputFilePath);
		Integer width = (Integer) imageMetadata.get(META_WIDTH);
		Integer height = (Integer) imageMetadata.get(META_HEIGHT);
		String outputFilePath = inputFilePath.getPath() + "_result";
		return performWatermarkOnFile(getDefaultWatermarkFile().getPath(),
				width, height, inputFilePath.getPath(), outputFilePath);
	}

	public File performWatermarkOnFile(String watermarkFilePath,
			Integer watermarkWidth, Integer watermarkHeight,
			String inputFilePath, String outputFilePath) throws Exception {
		File wtmkdFile = null;

		wtmkdFile = ImageWatermarker.watermark(getDefaultWatermarkFile()
				.getPath(), watermarkWidth, watermarkHeight, inputFilePath,
				outputFilePath);
		return wtmkdFile;
	}

	public File getDefaultWatermarkFile() throws IOException {
		if (defaultWatermarkFile == null) {
			defaultWatermarkFile = new File(System.getProperty("java.io.tmpdir"), UUID
					.randomUUID().toString());
			InputStream is = getClass().getClassLoader().getResourceAsStream(
					"watermark/image/dam_logo.png");
			FileUtils.copyToFile(is, defaultWatermarkFile);
			is.close();
			defaultWatermarkFile.deleteOnExit();
		}
		return defaultWatermarkFile;
	}

	protected Map<String, Object> getImageMetadata(File image)
			throws ClientException {
		Map<String, Object> imageMetadata = getImagingService()
				.getImageMetadata(new FileBlob(image));
		return imageMetadata;
	}

	protected ImagingService getImagingService() throws ClientException {
		if (imagingService == null) {
			try {
				imagingService = Framework.getService(ImagingService.class);
			} catch (Exception e) {
				log.error("Unable to get Imaging Service.", e);
			}
		}
		if (imagingService == null) {
			throw new ClientException("Unable to get Imaging Service: null");
		}
		return imagingService;
	}

}
