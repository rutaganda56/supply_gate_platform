import { Link } from "react-router-dom";
import { Linkedin, Instagram, Twitter } from "lucide-react";

export function WebsiteFooter() {
  return (
    <footer className="bg-[#1e4d5c] text-white">
      <div className="max-w-6xl mx-auto px-6 py-12">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8 mb-12">
          <div>
            <h3 className="font-semibold mb-4">Platform</h3>
            <ul className="space-y-2">
              <li>
                <Link to="/website/products" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Browse Products
                </Link>
              </li>
              <li>
                <Link to="/login" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Supplier Login
                </Link>
              </li>
              <li>
                <Link to="/login?type=industry" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Industry Worker Login
                </Link>
              </li>
              <li>
                <Link to="/signUp" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Register as Supplier
                </Link>
              </li>
              <li>
                <Link to="/industryDashBoard/register" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Register as Industry Worker
                </Link>
              </li>
            </ul>
          </div>
          <div>
            <h3 className="font-semibold mb-4">Resources</h3>
            <ul className="space-y-2">
              <li>
                <Link to="/website/support" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Support Center
                </Link>
              </li>
              <li>
                <Link to="/website/pricing" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Pricing Plans
                </Link>
              </li>
              <li>
                <Link to="/website/support" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Documentation
                </Link>
              </li>
              <li>
                <Link to="/website/support" className="text-gray-300 text-sm hover:text-white transition-colors">
                  API Access
                </Link>
              </li>
            </ul>
          </div>
          <div>
            <h3 className="font-semibold mb-4">Company</h3>
            <ul className="space-y-2">
              <li>
                <Link to="/website/support" className="text-gray-300 text-sm hover:text-white transition-colors">
                  About Us
                </Link>
              </li>
              <li>
                <Link to="/website/support" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Contact Sales
                </Link>
              </li>
              <li>
                <Link to="/website/support" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Careers
                </Link>
              </li>
              <li>
                <Link to="/website/support" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Blog
                </Link>
              </li>
            </ul>
          </div>
          <div>
            <h3 className="font-semibold mb-4">Legal & Support</h3>
            <ul className="space-y-2">
              <li>
                <Link to="/website/support" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Terms of Service
                </Link>
              </li>
              <li>
                <Link to="/website/support" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Privacy Policy
                </Link>
              </li>
              <li>
                <Link to="/website/support" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Security
                </Link>
              </li>
              <li>
                <Link to="/website/support" className="text-gray-300 text-sm hover:text-white transition-colors">
                  Help Center
                </Link>
              </li>
            </ul>
          </div>
        </div>
      </div>

      {/* Footer Bottom */}
      <div className="border-t border-gray-600">
        <div className="max-w-6xl mx-auto px-6 py-4 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex flex-wrap items-center gap-4 md:gap-6 text-sm">
            <span className="font-medium">© 2025 Supply Gate Platform. All rights reserved.</span>
            <Link to="/website/support" className="text-gray-300 hover:text-white transition-colors">
              Terms of Service
            </Link>
            <Link to="/website/support" className="text-gray-300 hover:text-white transition-colors">
              Privacy Policy
            </Link>
            <span className="text-gray-400">|</span>
            <span className="text-gray-300">Email: support@supplygate.com</span>
            <span className="text-gray-300">Phone: +250 788 123 456</span>
          </div>
          <div className="flex items-center gap-4">
            <a 
              href="https://linkedin.com/company/supplygate" 
              target="_blank"
              rel="noopener noreferrer"
              className="hover:opacity-80 transition-opacity"
              aria-label="LinkedIn"
            >
              <Linkedin size={20} />
            </a>
            <a 
              href="https://instagram.com/supplygate" 
              target="_blank"
              rel="noopener noreferrer"
              className="hover:opacity-80 transition-opacity"
              aria-label="Instagram"
            >
              <Instagram size={20} />
            </a>
            <a 
              href="https://twitter.com/supplygate" 
              target="_blank"
              rel="noopener noreferrer"
              className="hover:opacity-80 transition-opacity"
              aria-label="Twitter"
            >
              <Twitter size={20} />
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}
