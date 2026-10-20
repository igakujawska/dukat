package com.example.demoapp.controller;

import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService service;

  public CategoryController(CategoryService service) {
    this.service = service;
  }

  @GetMapping
  public List<CategoryResponse> list(
    @RequestParam(defaultValue = "false") boolean includeInactive
  ) {
    return service.list(includeInactive);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CategoryResponse create(@Valid @RequestBody CategoryRequest r) {
    return service.create(r);
  }

  @PutMapping("/{id}")
  public CategoryResponse update(
    @PathVariable Long id,
    @Valid @RequestBody CategoryRequest r
  ) {
    return service.update(id, r);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Wyłącz kategorię; historyczne transakcje zachowują poprawny klucz obcy"
  )
  public void archive(@PathVariable Long id) {
    service.archive(id);
  }
}
