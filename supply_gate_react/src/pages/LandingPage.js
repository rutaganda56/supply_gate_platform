import React, { useState, useEffect } from "react";
import { Search, ArrowRight } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "../components/ui/button";
import { WebsiteFooter } from "../components/WebsiteFooter";
import { WebsiteHeader } from "../components/WebsiteHeader";

export default function LandingPage() {
  const [displayText, setDisplayText] = useState("");
  const fullText = "best";
  const [searchQuery, setSearchQuery] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    let index = 0;
    const interval = setInterval(() => {
      if (index <= fullText.length) {
        setDisplayText(fullText.slice(0, index));
        index++;
      } else {
        index = 0;
      }
    }, 300);
    return () => clearInterval(interval);
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/website/products?search=${encodeURIComponent(searchQuery)}`);
    }
  };

  return (
    <div className="min-h-screen bg-white">
      <WebsiteHeader activePage="home" />

      <section className="px-6 py-6 border-b border-gray-100 bg-gray-50">
        <div className="max-w-4xl mx-auto">
          <form onSubmit={handleSearch} className="relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
            <input
              type="text"
              placeholder="Search for products, suppliers, categories..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-12 pr-4 py-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#1e4d5c] focus:border-transparent text-gray-900 placeholder:text-gray-500"
            />
            <Button type="submit" className="absolute right-2 top-1/2 -translate-y-1/2 bg-[#1e4d5c] hover:bg-[#163d49] text-white">
              Search
            </Button>
          </form>
        </div>
      </section>

      {/* Hero Section */}
      <section className="px-6 py-12 md:py-20">
        <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center gap-8 ">
          <div className="flex-1">
            <h1 className="text-3xl md:text-4xl font-bold text-gray-900 leading-tight mb-4">
              Take Supply From
              <br />
              better to {displayText}
              <span className="inline-block w-0.5 h-8 bg-gray-400 ml-1 animate-pulse" />
            </h1>
            <p className="text-gray-600 text-sm leading-relaxed max-w-md">
              Supply Gate is a trusted online marketplace designed to connect businesses with verified manufacturers and
              suppliers.
            </p>
          </div>
        </div>
      </section>

      {/* Trust Section */}
      <section className="px-6 py-12 border-t border-gray-100">
        <div className="max-w-4xl mx-auto text-center">
          <p className="text-gray-600 text-sm mb-8">Trusted by over 5k users</p>
        </div>
      </section>

      {/* Stats Section */}
      <section className="bg-[#1e4d5c] px-6 py-12">
        <div className="max-w-4xl mx-auto">
          <div className="mb-8">
            <h2 className="text-2xl md:text-3xl font-bold text-white italic mb-2">
              Why Suppliers trust
              <br />
              supplyGate
            </h2>
            <Link to="/website/support" className="text-white text-sm underline hover:no-underline">
              Contact sales to request a demo
            </Link>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-8 text-white">
            <div className="text-center">
              <p className="text-2xl md:text-3xl font-bold italic mb-2">ISO</p>
              <p className="text-sm text-gray-300">Certified Platform</p>
            </div>
            <div className="text-center">
              <p className="text-2xl md:text-3xl font-bold mb-2">#1</p>
              <p className="text-sm text-gray-300">Supply Chain Platform</p>
            </div>
            <div className="text-center">
              <p className="text-2xl md:text-3xl font-bold mb-2">99%</p>
              <p className="text-sm text-gray-300">Verified Suppliers</p>
            </div>
            <div className="text-center">
              <p className="text-2xl md:text-3xl font-bold mb-2">10+</p>
              <p className="text-sm text-gray-300">Active Suppliers</p>
            </div>
            <div className="text-center">
              <p className="text-2xl md:text-3xl font-bold mb-2">5+</p>
              <p className="text-sm text-gray-300">Industry Partners</p>
            </div>
            <div className="text-center">
              <p className="text-2xl md:text-3xl font-bold mb-2">24/7</p>
              <p className="text-sm text-gray-300">Support Available</p>
            </div>
          </div>
        </div>
      </section>

      {/* Testimonial Section */}
      <section className="px-6 py-16 bg-white">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-2xl md:text-3xl font-bold text-gray-900 text-center mb-2">
            Loved by rwandan
            <br />
            best companies
          </h2>
          <p className="text-2xl md:text-3xl font-bold text-gray-900 text-center mb-8">KABISA</p>

          <div className="max-w-md mx-auto">
            <p className="text-gray-600 text-sm leading-relaxed mb-6">
              "Supply Gate has transformed how we manage our supply chain. The verification process ensures we only work with trusted suppliers, and the platform makes it easy to find exactly what we need. It's been a game-changer for our business operations."
            </p>
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-gray-200 overflow-hidden">
              </div>
              <div>
                <p className="font-semibold text-gray-900 text-sm">Alice Mutoni</p>
                <p className="text-gray-500 text-xs">Managing Director at Kabisa</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="px-6 py-12">
        <div className="max-w-2xl mx-auto bg-[#1e4d5c] rounded-2xl px-8 py-12 text-center">
          <h2 className="text-2xl md:text-3xl font-bold text-white mb-2">Join 5k+ users today</h2>
          <p className="text-gray-300 text-sm mb-2">Start for free — upgrade anytime.</p>
          <Link to="/website/support" className="text-white text-sm underline hover:no-underline mb-6 inline-block">
            Joining as an organization? Contact Sales
          </Link>
          <div className="mt-4">
            <Button
              asChild
              variant="outline"
              className="bg-[#1e4d5c] border-white text-white hover:bg-[#163d49] hover:text-white"
            >
              <Link to="/signUp" className="flex items-center gap-2">
                Sign up free <ArrowRight size={16} />
              </Link>
            </Button>
          </div>
        </div>
      </section>

      {/* Footer */}
      <WebsiteFooter />
    </div>
  );
}
