import React from "react";
import { Globe, ArrowRight } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "./ui/button";

export function WebsiteHeader({ activePage = "home" }) {
  return (
    <header className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-white">
      <div className="flex items-center gap-8">
        <Link to="/" className="text-xl font-semibold text-gray-900">
          Supply Gate
        </Link>
        <nav className="hidden md:flex items-center gap-6">
          <Link 
            to="/" 
            className={`text-sm hover:text-gray-900 ${
              activePage === "home" ? "text-[#1e4d5c] font-semibold" : "text-gray-700"
            }`}
          >
            Home
          </Link>
          <Link 
            to="/website/products" 
            className={`text-sm hover:text-gray-900 ${
              activePage === "products" ? "text-[#1e4d5c] font-semibold" : "text-gray-700"
            }`}
          >
            Products
          </Link>
          <Link 
            to="/website/pricing" 
            className={`text-sm hover:text-gray-900 ${
              activePage === "pricing" ? "text-[#1e4d5c] font-semibold" : "text-gray-700"
            }`}
          >
            Pricing
          </Link>
          <Link 
            to="/website/support" 
            className={`text-sm hover:text-gray-900 ${
              activePage === "support" ? "text-[#1e4d5c] font-semibold" : "text-gray-700"
            }`}
          >
            Support
          </Link>
        </nav>
      </div>
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" className="hidden sm:flex items-center gap-1 text-sm text-gray-600">
          <Globe size={16} />
          EN
        </Button>
        <Link to="/login" className="text-sm text-gray-700 hover:text-gray-900">
          login
        </Link>
        <Button asChild className="bg-[#1e4d5c] hover:bg-[#163d49] text-white">
          <Link to="/signUp" className="flex items-center gap-2">
            sign up <ArrowRight size={16} />
          </Link>
        </Button>
      </div>
    </header>
  );
}

