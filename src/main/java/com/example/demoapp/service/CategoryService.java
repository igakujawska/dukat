package com.example.demoapp.service;

import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.model.*;
import com.example.demoapp.repository.CategoryRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CategoryService {

  private final CategoryRepository categories;
  private final CurrentUser current;

  public CategoryService(CategoryRepository categories, CurrentUser current) {
    this.categories = categories;
    this.current = current;
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> list(boolean includeInactive) {
    return categories
      .findAllByOwnerIdOrderByIdAsc(current.id())
      .stream()
      .filter(c -> includeInactive || c.isActive())
      .map(CategoryService::response)
      .toList();
  }

  public CategoryResponse create(CategoryRequest request) {
    var owner = current.required();
    if (
      categories
        .findByOwnerIdAndNameIgnoreCase(owner.getId(), request.name().trim())
        .isPresent()
    ) throw new ResponseStatusException(
      HttpStatus.CONFLICT,
      "Taka kategoria już istnieje"
    );
    var category = new Category(request.name().trim());
    category.setOwner(owner);
    return response(categories.save(category));
  }

  public CategoryResponse update(Long id, CategoryRequest request) {
    var category = owned(id, current.id());
    var duplicate = categories.findByOwnerIdAndNameIgnoreCase(
      current.id(),
      request.name().trim()
    );
    if (
      duplicate.isPresent() && !duplicate.get().getId().equals(id)
    ) throw new ResponseStatusException(
      HttpStatus.CONFLICT,
      "Taka kategoria już istnieje"
    );
    category.setName(request.name().trim());
    category.setActive(true);
    return response(categories.save(category));
  }

  public void archive(Long id) {
    var category = owned(id, current.id());
    category.setActive(false);
  }

  public Category owned(Long id, Long owner) {
    return categories
      .findByIdAndOwnerId(id, owner)
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Nie znaleziono kategorii"
        )
      );
  }

  public Category active(Long id, Long owner) {
    var category = owned(id, owner);
    if (!category.isActive()) throw new IllegalArgumentException(
      "Wybierz aktywną kategorię"
    );
    return category;
  }

  public void seed(User owner) {
    for (String name : List.of(
      "Jedzenie i Restauracje",
      "Mieszkanie i Rachunki",
      "Transport",
      "Rozrywka",
      "Wynagrodzenie",
      "Inne",
      "Przelewy"
    )) {
      if (
        categories.findByOwnerIdAndNameIgnoreCase(owner.getId(), name).isEmpty()
      ) {
        var c = new Category(name);
        c.setOwner(owner);
        categories.save(c);
      }
    }
  }

  public Category transferCategory(User owner) {
    return categories
      .findByOwnerIdAndNameIgnoreCase(owner.getId(), "Przelewy")
      .orElseGet(() -> {
        var category = new Category("Przelewy");
        category.setOwner(owner);
        return categories.save(category);
      });
  }

  public static CategoryResponse response(Category c) {
    return new CategoryResponse(c.getId(), c.getName(), c.isActive());
  }
}
