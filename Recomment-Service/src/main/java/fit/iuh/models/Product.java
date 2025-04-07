package fit.iuh.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product")
public class Product {
    @Id
    private Long id;
    private String name;
    private double price;
    private String useCase;
    private String performance;

    // Constructors
    public Product() {}

    public Product(Long id, String name, double price, String useCase, String performance) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.useCase = useCase;
        this.performance = performance;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getUseCase() { return useCase; }
    public void setUseCase(String useCase) { this.useCase = useCase; }
    public String getPerformance() { return performance; }
    public void setPerformance(String performance) { this.performance = performance; }
}
