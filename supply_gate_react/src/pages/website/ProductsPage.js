import { useState, useEffect, useCallback } from "react";
import { MessageCircle, Heart, Check, Search, Loader2 } from "lucide-react";
import { Link, useSearchParams } from "react-router-dom";
import { Button } from "../../components/ui/button";
import { WebsiteFooter } from "../../components/WebsiteFooter";
import { WebsiteHeader } from "../../components/WebsiteHeader";
import { productApi } from "../../lib/api";
import { ContactSupplierDialog } from "../../components/ContactSupplierDialog";
import { showWarning } from "../../lib/toast";

const API_BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:8080";

export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [showVerifiedOnly, setShowVerifiedOnly] = useState(false);
  const [likedProducts, setLikedProducts] = useState(new Set());
  const [searchQuery, setSearchQuery] = useState(searchParams.get("search") || "");
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [pageSize] = useState(12); // Fixed page size for products
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [contactDialogOpen, setContactDialogOpen] = useState(false);
  const [selectedSupplier, setSelectedSupplier] = useState(null);

  const loadProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      // Pass verifiedOnly parameter to backend
      // If false (default), shows all products in the system
      // If true, filters to only show products from verified suppliers
      const response = await productApi.getAllProducts(page, pageSize, searchQuery || undefined, showVerifiedOnly);
      
      setProducts(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err) {
      console.error("Failed to load products:", err);
      setError("Failed to load products. Please try again later.");
      setProducts([]);
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, searchQuery, showVerifiedOnly]);

  // Fetch products from API
  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  // Track product views when products are displayed
  // Modern apps track views for analytics (Instagram, Facebook, Amazon)
  useEffect(() => {
    if (products.length > 0) {
      // Track views for all visible products (debounced to avoid too many calls)
      const timeoutId = setTimeout(() => {
        products.forEach((product) => {
          if (product.productId) {
            productApi.trackProductView(product.productId).catch(() => {
              // Silently fail - tracking should not break UX
            });
          }
        });
      }, 1000); // Wait 1 second after products load to track views

      return () => clearTimeout(timeoutId);
    }
  }, [products]);

  // Products are already filtered by backend based on showVerifiedOnly parameter
  const filteredProducts = products;

  const toggleLike = async (productId) => {
    const isCurrentlyLiked = likedProducts.has(productId);
    const newLikedState = !isCurrentlyLiked;
    
    // Optimistically update UI
    setLikedProducts((prev) => {
      const newSet = new Set(prev);
      if (newLikedState) {
        newSet.add(productId);
      } else {
        newSet.delete(productId);
      }
      return newSet;
    });

    // Track the like/unlike event in backend (non-blocking)
    // Modern apps do this for analytics (Instagram, Pinterest, etc.)
    productApi.trackProductLike(productId, newLikedState).catch((err) => {
      // Silently fail - don't break user experience if tracking fails
      if (process.env.NODE_ENV === 'development') {
        console.warn("Failed to track product like:", err);
      }
    });
  };
  
  const formatPrice = (price) => {
    if (price === undefined || price === null) return "Price not available";
    return `$${price.toFixed(2)}`;
  };
  
  const getProductImage = (product) => {
    if (product.imageUrls && product.imageUrls.length > 0) {
      // Construct full URL for backend images
      const imageUrl = product.imageUrls[0];
      
      // If already a full URL (starts with http), return as-is
      if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
        return imageUrl;
      }
      
      // If it's a relative path (e.g., "products/uuid-filename.jpg"), use ImageController
      // The ImageController serves images at /api/images?path=...
      if (imageUrl.includes("/") || imageUrl.includes("\\")) {
        // Encode the path for URL
        const encodedPath = encodeURIComponent(imageUrl);
        return `${API_BASE_URL}/api/images?path=${encodedPath}`;
      }
      
      // Fallback: try direct path
      return `${API_BASE_URL}${imageUrl.startsWith("/") ? "" : "/"}${imageUrl}`;
    }
    return "/placeholder.svg";
  };

  const handleSearch = (e) => {
    e.preventDefault();
    // Update URL with search query
    if (searchQuery.trim()) {
      setSearchParams({ search: searchQuery.trim() });
    } else {
      setSearchParams({});
    }
    setPage(0); // Reset to first page when searching
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <WebsiteHeader activePage="products" />

      {/* Products Section */}
      <section className="px-6 py-12">
        <div className="max-w-6xl mx-auto">
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900 mb-4">Browse Products</h1>

            <form onSubmit={handleSearch} className="relative mb-4">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
              <input
                type="text"
                placeholder="Search products, suppliers, categories..."
                value={searchQuery}
                onChange={(e) => {
                  setSearchQuery(e.target.value);
                }}
                className="w-full pl-12 pr-4 py-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#1e4d5c] focus:border-transparent text-gray-900 placeholder:text-gray-500 bg-white"
              />
            </form>

            {/* Filter */}
            <div className="flex items-center gap-3 bg-white p-4 rounded-lg border border-gray-200">
              <label className="flex items-center gap-2 cursor-pointer">
                <div className="relative">
                  <input
                    type="checkbox"
                    checked={showVerifiedOnly}
                    onChange={(e) => {
                      setShowVerifiedOnly(e.target.checked);
                      setPage(0); // Reset to first page when filter changes
                    }}
                    className="sr-only"
                  />
                  <div
                    className={`w-5 h-5 border-2 rounded flex items-center justify-center transition-colors ${
                      showVerifiedOnly ? "bg-[#1e4d5c] border-[#1e4d5c]" : "bg-white border-gray-300"
                    }`}
                  >
                    {showVerifiedOnly && <Check size={14} className="text-white" />}
                  </div>
                </div>
                <span className="text-sm text-gray-700">Show verified suppliers only</span>
              </label>
            </div>
          </div>

          <div className="mb-4">
            <p className="text-sm text-gray-600">
              {filteredProducts.length} {filteredProducts.length === 1 ? "product" : "products"} found
              {searchQuery && <span className="font-semibold"> for "{searchQuery}"</span>}
            </p>
          </div>

          {/* Loading State */}
          {loading && (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="w-8 h-8 animate-spin text-[#1e4d5c]" />
              <span className="ml-3 text-gray-600">Loading products...</span>
            </div>
          )}

          {/* Error State */}
          {error && !loading && (
            <div className="text-center py-12">
              <p className="text-red-600 mb-4">{error}</p>
              <Button onClick={loadProducts} className="bg-[#1e4d5c] hover:bg-[#163d49] text-white">
                Try Again
              </Button>
            </div>
          )}

          {/* Product Grid */}
          {!loading && !error && (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredProducts.map((product) => (
                  <div key={product.productId} className="bg-white rounded-lg border border-gray-200 overflow-hidden shadow-sm hover:shadow-md transition-shadow">
                    <div className="relative">
                      <img
                        src={getProductImage(product)}
                        alt={product.productName}
                        className="w-full h-48 object-cover"
                        onError={(e) => {
                          // Fallback to placeholder on error
                          const target = e.target;
                          if (target.src !== "/placeholder.svg") {
                            target.src = "/placeholder.svg";
                          }
                        }}
                      />
                      <Button
                        variant="secondary"
                        size="icon"
                        onClick={() => product.productId && toggleLike(product.productId)}
                        className="absolute top-3 right-3 bg-white rounded-full shadow-md hover:scale-110 transition-transform"
                      >
                        <Heart
                          size={18}
                          className={product.productId && likedProducts.has(product.productId) ? "fill-red-500 text-red-500" : "text-gray-600"}
                        />
                      </Button>
                    </div>

                    <div className="p-4">
                      <div className="flex items-start justify-between mb-2">
                        <h3 className="font-semibold text-gray-900 text-lg">{product.productName}</h3>
                      </div>

                      <div className="flex items-center gap-2 mb-2">
                        <p className="text-sm text-gray-600">{product.supplierName || product.storeName || "Unknown Supplier"}</p>
                        {product.isSupplierVerified && (
                          <span className="bg-green-100 text-green-700 text-xs px-2 py-0.5 rounded-full flex items-center gap-1">
                            <Check size={12} />
                            Verified
                          </span>
                        )}
                      </div>

                      {product.categoryName && (
                        <p className="text-xs text-gray-500 mb-1">{product.categoryName}</p>
                      )}

                      <p className="text-gray-600 text-sm mb-3 line-clamp-2">
                        {product.productDescription || "No description available"}
                      </p>

                      <div className="flex items-center justify-between mb-3">
                        <p className="text-[#1e4d5c] font-bold text-lg">
                          {formatPrice(product.productPrice)}
                        </p>
                        {product.quantity && (
                          <p className="text-sm text-gray-500">Qty: {product.quantity}</p>
                        )}
                      </div>

                      <Button 
                        className="w-full mt-4 bg-[#1e4d5c] hover:bg-[#163d49] text-white flex items-center gap-2"
                        onClick={() => {
                          if (product.supplierId) {
                            setSelectedSupplier({
                              supplierId: product.supplierId,
                              supplierName: product.supplierName,
                              supplierEmail: product.supplierEmail,
                              productId: product.productId,
                              productName: product.productName,
                            });
                            setContactDialogOpen(true);
                          } else {
                            showWarning("Supplier information not available for this product.");
                          }
                        }}
                      >
                        <MessageCircle size={16} />
                        Message Supplier
                      </Button>
                    </div>
                  </div>
                ))}
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-center gap-2 mt-8">
                  <Button
                    variant="outline"
                    onClick={() => setPage(Math.max(0, page - 1))}
                    disabled={page === 0 || loading}
                  >
                    Previous
                  </Button>
                  <span className="text-sm text-gray-600">
                    Page {page + 1} of {totalPages} ({totalElements} total)
                  </span>
                  <Button
                    variant="outline"
                    onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                    disabled={page >= totalPages - 1 || loading}
                  >
                    Next
                  </Button>
                </div>
              )}

              {/* Empty State */}
              {filteredProducts.length === 0 && !loading && !error && (
                <div className="text-center py-12">
                  <p className="text-gray-500">
                    No products found{searchQuery && ` for "${searchQuery}"`} with the current filters.
                  </p>
                  {searchQuery && (
                    <Button variant="link" onClick={() => setSearchQuery("")} className="mt-4 text-[#1e4d5c]">
                      Clear search
                    </Button>
                  )}
                </div>
              )}
            </>
          )}
        </div>
      </section>

      {/* Footer */}
      <WebsiteFooter />

      {/* Contact Supplier Dialog */}
      {selectedSupplier && (
        <ContactSupplierDialog
          open={contactDialogOpen}
          onClose={() => {
            setContactDialogOpen(false);
            setSelectedSupplier(null);
          }}
          supplierId={selectedSupplier.supplierId}
          supplierName={selectedSupplier.supplierName}
          supplierEmail={selectedSupplier.supplierEmail}
          productId={selectedSupplier.productId}
          productName={selectedSupplier.productName}
        />
      )}
    </div>
  );
}
