import { Mail, Phone, MessageCircle } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "../../components/ui/button";
import { WebsiteFooter } from "../../components/WebsiteFooter";
import { WebsiteHeader } from "../../components/WebsiteHeader";

export default function SupportPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <WebsiteHeader activePage="support" />

      {/* Support Section */}
      <section className="px-6 py-16">
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold text-gray-900 mb-4">How can we help you?</h1>
            <p className="text-gray-600 text-lg">Get in touch with our support team</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
            {/* Email Support */}
            <div className="bg-white rounded-lg border border-gray-200 p-6 text-center">
              <div className="w-12 h-12 bg-[#1e4d5c] rounded-full flex items-center justify-center mx-auto mb-4">
                <Mail size={24} className="text-white" />
              </div>
              <h3 className="font-semibold text-gray-900 mb-2">Email Support</h3>
              <p className="text-gray-600 text-sm mb-4">We'll respond within 24 hours</p>
              <a href="mailto:support@supplygate.com" className="text-[#1e4d5c] text-sm font-semibold hover:underline">
                support@supplygate.com
              </a>
            </div>

            {/* Phone Support */}
            <div className="bg-white rounded-lg border border-gray-200 p-6 text-center">
              <div className="w-12 h-12 bg-[#1e4d5c] rounded-full flex items-center justify-center mx-auto mb-4">
                <Phone size={24} className="text-white" />
              </div>
              <h3 className="font-semibold text-gray-900 mb-2">Phone Support</h3>
              <p className="text-gray-600 text-sm mb-4">Mon-Fri, 9am-5pm EAT</p>
              <a href="tel:+250788123456" className="text-[#1e4d5c] text-sm font-semibold hover:underline">
                +250 788 123 456
              </a>
            </div>

            {/* Live Chat */}
            <div className="bg-white rounded-lg border border-gray-200 p-6 text-center">
              <div className="w-12 h-12 bg-[#1e4d5c] rounded-full flex items-center justify-center mx-auto mb-4">
                <MessageCircle size={24} className="text-white" />
              </div>
              <h3 className="font-semibold text-gray-900 mb-2">Live Chat</h3>
              <p className="text-gray-600 text-sm mb-4">Available during business hours</p>
              <Button variant="link" className="text-[#1e4d5c]">
                Start Chat
              </Button>
            </div>
          </div>

          {/* FAQ Section */}
          <div className="bg-white rounded-lg border border-gray-200 p-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-6">Frequently Asked Questions</h2>

            <div className="space-y-6">
              <div>
                <h3 className="font-semibold text-gray-900 mb-2">How do I verify my supplier account?</h3>
                <p className="text-gray-600 text-sm leading-relaxed">
                  To verify your supplier account, you need to submit your business registration documents, tax
                  identification number, and proof of business address. Our team will review your documents within 2-3
                  business days.
                </p>
              </div>

              <div>
                <h3 className="font-semibold text-gray-900 mb-2">What payment methods do you accept?</h3>
                <p className="text-gray-600 text-sm leading-relaxed">
                  We accept major credit cards, bank transfers, and mobile money payments. Enterprise customers can also
                  arrange for invoice-based billing.
                </p>
              </div>

              <div>
                <h3 className="font-semibold text-gray-900 mb-2">Can I cancel my subscription anytime?</h3>
                <p className="text-gray-600 text-sm leading-relaxed">
                  Yes, you can cancel your subscription at any time. Your access will continue until the end of your
                  current billing period, and you won't be charged again.
                </p>
              </div>

              <div>
                <h3 className="font-semibold text-gray-900 mb-2">How do I contact a supplier?</h3>
                <p className="text-gray-600 text-sm leading-relaxed">
                  Click the "Message Supplier" button on any product card. You'll need to be logged in to send messages.
                  Free plan users get up to 10 messages per month, while Professional and Enterprise users have
                  unlimited messaging.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <WebsiteFooter />
    </div>
  );
}
