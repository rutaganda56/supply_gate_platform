import React, { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "./ui/dialog";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Textarea } from "./ui/textarea";
import { MessageCircle, Send, Loader2, CheckCircle2 } from "lucide-react";
import { messageApi } from "../lib/api";

export function ContactSupplierDialog({
  open,
  onClose,
  supplierId,
  supplierName,
  supplierEmail,
  productId,
  productName,
}) {
  const [formData, setFormData] = useState({
    senderName: "",
    senderEmail: "",
    senderPhone: "",
    subject: "",
    messageContent: "",
  });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // Validate message content to prevent spam/empty messages
      if (!formData.messageContent.trim() || formData.messageContent.trim().length < 10) {
        setError("Message must be at least 10 characters long.");
        setLoading(false);
        return;
      }

      const messageData = {
        supplierId,
        senderName: formData.senderName.trim(),
        senderEmail: formData.senderEmail.trim(),
        senderPhone: formData.senderPhone.trim() || undefined,
        subject: formData.subject.trim(),
        messageContent: formData.messageContent.trim(),
        productId: productId || undefined,
        productName: productName || undefined,
      };

      // Use the API function instead of direct axios call
      await messageApi.sendMessage(messageData);

      setSuccess(true);
      // Reset form
      setFormData({
        senderName: "",
        senderEmail: "",
        senderPhone: "",
        subject: "",
        messageContent: "",
      });
      // Close dialog after 2 seconds
      setTimeout(() => {
        setSuccess(false);
        onClose();
      }, 2000);
    } catch (err) {
      console.error("Failed to send message:", err);
      const errorMessage = err.response?.data?.message || 
                          err.response?.data?.error || 
                          err.message ||
                          "Failed to send message. Please try again.";
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setFormData({
        senderName: "",
        senderEmail: "",
        senderPhone: "",
        subject: "",
        messageContent: "",
      });
      setError(null);
      setSuccess(false);
      onClose();
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold text-gray-800 flex items-center gap-2">
            <MessageCircle className="w-5 h-5 text-[#1e4d5c]" />
            Contact Supplier
          </DialogTitle>
          <DialogDescription className="text-sm text-gray-600">
            {supplierName
              ? `Send a message to ${supplierName}. The supplier will receive an email notification and can reply directly via email.`
              : "Send a message to this supplier. The supplier will receive an email notification and can reply directly via email."}
            {productName && (
              <span className="block mt-1">Regarding: {productName}</span>
            )}
          </DialogDescription>
        </DialogHeader>

        {success ? (
          <div className="flex flex-col items-center justify-center py-8">
            <CheckCircle2 className="w-16 h-16 text-green-600 mb-4" />
            <h3 className="text-lg font-semibold text-gray-800 mb-2">
              Message Sent Successfully!
            </h3>
            <p className="text-sm text-gray-600 text-center">
              Your message has been delivered to the supplier. They will receive
              a notification and can reply to you via email.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4 mt-4">
            {/* Sender Name */}
            <div className="space-y-2">
              <Label htmlFor="senderName" className="text-sm font-medium text-gray-700">
                Your Name <span className="text-red-500">*</span>
              </Label>
              <Input
                id="senderName"
                type="text"
                value={formData.senderName}
                onChange={(e) =>
                  setFormData({ ...formData, senderName: e.target.value })
                }
                required
                minLength={2}
                maxLength={100}
                placeholder="Enter your full name"
                className="border-gray-300"
                disabled={loading}
              />
            </div>

            {/* Sender Email */}
            <div className="space-y-2">
              <Label htmlFor="senderEmail" className="text-sm font-medium text-gray-700">
                Your Email <span className="text-red-500">*</span>
              </Label>
              <Input
                id="senderEmail"
                type="email"
                value={formData.senderEmail}
                onChange={(e) =>
                  setFormData({ ...formData, senderEmail: e.target.value })
                }
                required
                placeholder="your.email@example.com"
                className="border-gray-300"
                disabled={loading}
              />
            </div>

            {/* Sender Phone (Optional) */}
            <div className="space-y-2">
              <Label htmlFor="senderPhone" className="text-sm font-medium text-gray-700">
                Phone Number <span className="text-gray-500 text-xs">(Optional)</span>
              </Label>
              <Input
                id="senderPhone"
                type="tel"
                value={formData.senderPhone}
                onChange={(e) =>
                  setFormData({ ...formData, senderPhone: e.target.value })
                }
                placeholder="+1234567890"
                className="border-gray-300"
                disabled={loading}
              />
            </div>

            {/* Subject */}
            <div className="space-y-2">
              <Label htmlFor="subject" className="text-sm font-medium text-gray-700">
                Subject <span className="text-red-500">*</span>
              </Label>
              <Input
                id="subject"
                type="text"
                value={formData.subject}
                onChange={(e) =>
                  setFormData({ ...formData, subject: e.target.value })
                }
                required
                minLength={5}
                maxLength={200}
                placeholder="What is your message about?"
                className="border-gray-300"
                disabled={loading}
              />
            </div>

            {/* Message Content */}
            <div className="space-y-2">
              <Label htmlFor="messageContent" className="text-sm font-medium text-gray-700">
                Message <span className="text-red-500">*</span>
              </Label>
              <Textarea
                id="messageContent"
                value={formData.messageContent}
                onChange={(e) =>
                  setFormData({ ...formData, messageContent: e.target.value })
                }
                required
                minLength={10}
                maxLength={5000}
                rows={6}
                placeholder="Enter your message here. The supplier will receive this via email and can reply directly to your email address."
                className="border-gray-300 resize-none"
                disabled={loading}
              />
              <p className="text-xs text-gray-500">
                {formData.messageContent.length}/5000 characters
              </p>
            </div>

            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
                {error}
              </div>
            )}

            <DialogFooter className="mt-6">
              <Button
                type="button"
                variant="outline"
                onClick={handleClose}
                disabled={loading}
                className="border-gray-300"
              >
                Cancel
              </Button>
              <Button
                type="submit"
                disabled={loading}
                className="bg-[#1e4d5c] hover:bg-[#163d49] text-white flex items-center gap-2"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Sending...
                  </>
                ) : (
                  <>
                    <Send className="w-4 h-4" />
                    Send Message
                  </>
                )}
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
}
