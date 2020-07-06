package org.comeia.project.controller;

import static org.comeia.project.search.DocumentSpecification.listAllByCriteria;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.comeia.project.converter.DocumentConverter;
import org.comeia.project.domain.Document;
import org.comeia.project.dto.DocumentDTO;
import org.comeia.project.dto.DocumentFilterDTO;
import org.comeia.project.enumerator.DocumentType;
import org.comeia.project.locale.ErrorMessageKeys;
import org.comeia.project.repository.DocumentRepository;
import org.comeia.project.search.SearchCriteria;
import org.comeia.project.service.StorageService;
import org.comeia.project.validator.DocumentValidator;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(value = "/api/v1/document")
@AllArgsConstructor
public class DocumentController extends ResourceController {
	
	private static final String SERVER_LOCATION = "documents";

	private final DocumentRepository repository;
	private final DocumentConverter converter;
	private final DocumentValidator validator;
	private final StorageService storageService;
	
	@GetMapping
	public MappingJacksonValue listByCriteria(Pageable pageable,
			@RequestParam(required = false) String attributes,
			DocumentFilterDTO filter) {
		
		List<SearchCriteria> criterias = DocumentFilterDTO.buildCriteria(filter);
		Page<DocumentDTO> pages = this.repository.findAll(listAllByCriteria(criterias), pageable)
				.map(converter::from);
		return buildResponse(pages, attributes);
	}
	
	@GetMapping(path = "/types")
	public MappingJacksonValue types(@RequestParam(required = false) String attributes) {
		DocumentType[] types = DocumentType.values();
		return buildResponse(types, attributes);
	}
	
	@GetMapping(path = "{id}")
	public MappingJacksonValue getById(@PathVariable long id,
			@RequestParam(required = false) String attributes) {
		
		DocumentDTO dto = this.repository.findByIdAndDeletedIsFalse(id)
				.map(this.converter::from)
				.orElseThrow(() -> throwsException(ErrorMessageKeys.ERROR_DOCUMENT_NOT_FOUND_BY_ID, String.valueOf(id)));
		return buildResponse(dto, attributes);
	}
	
	@PostMapping
	public MappingJacksonValue create(@Validated @RequestBody DocumentDTO dto,
			@RequestParam(required = false) String attributes) {
		
		if(Objects.isNull(dto)) {
			throw new HttpMessageNotReadableException("Required request body is missing");
		}
		
		DocumentDTO docDTO = Optional.of(dto)
				.map(this.converter::to)
				.map(this.repository::save)
				.map(this.converter::from)
				.orElseThrow(() -> throwsException("Error"));
		
		
		return buildResponse(docDTO, attributes);
	}
	
	@PostMapping("/upload")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) {

		storageService.store(file);
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");

		return "redirect:/";
	}
	
	@RequestMapping(path = "/download", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(@RequestParam("file") String document) throws IOException {
        File file = new File(SERVER_LOCATION + File.separator + document);

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document);
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");
        
        System.out.println(file.getAbsolutePath());

        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        return ResponseEntity.ok()
                .headers(header)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }
	
	//Route not being used!
	@PutMapping(path = "{id}")
	public MappingJacksonValue update(@PathVariable long id,
			@Validated @RequestBody DocumentDTO dto,
			@RequestParam(required = false) String attributes) {
		
		if(Objects.isNull(dto)) {
			throw new HttpMessageNotReadableException("Required request body is missing");
		}
		
		DocumentDTO docDTO = this.repository.findByIdAndDeletedIsFalse(id)
				.map(document -> this.converter.to(dto, document))
				.map(this.repository::save)
				.map(this.converter::from)
				.orElseThrow(() -> throwsException(String.valueOf(id)));
		
		return buildResponse(docDTO, attributes);
	}
	
	@DeleteMapping(path = "{id}")
	public void delete(@PathVariable long id) {
		
		DocumentDTO dto = this.repository.findByIdAndDeletedIsFalse(id)
				.map(this.converter::from)
				.orElseThrow(() -> throwsException(ErrorMessageKeys.ERROR_DOCUMENT_NOT_FOUND_BY_ID, String.valueOf(id)));
		
		Document document = this.converter.to(dto);
		document.setDeleted(true);
		document.setId(dto.getId());
		this.repository.save(document);
		
		String path = "documents/" + document.getDirectory();
		File file = new File(path);
		file.delete();
		
	}
}
