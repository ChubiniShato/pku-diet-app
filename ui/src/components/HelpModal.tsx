import React from 'react'
import { useHelp } from '@/contexts/HelpContext'
import { Modal } from './Modal'
import { Button } from './Button'

export const HelpModal: React.FC = () => {
  const { isHelpVisible, hideHelp, currentHelpContent } = useHelp()

  if (!currentHelpContent) return null

  return (
    <Modal
      isOpen={isHelpVisible}
      onClose={hideHelp}
      title={currentHelpContent.title}
      size="lg"
    >
      <div className="space-y-6">
        {/* Description */}
        <div className="bg-blue-50 p-4 rounded-lg">
          <p className="text-blue-800 text-sm leading-relaxed">
            {currentHelpContent.description}
          </p>
        </div>

        {/* Sections */}
        <div className="space-y-4">
          {currentHelpContent.sections.map((section, index) => (
            <div key={index} className="border-l-4 border-blue-500 pl-4">
              <h3 className="text-lg font-semibold text-gray-900 mb-2">
                {section.title}
              </h3>
              <p className="text-gray-700 mb-3">
                {section.content}
              </p>
              {section.items && (
                <ul className="space-y-1">
                  {section.items.map((item, itemIndex) => (
                    <li key={itemIndex} className="flex items-start">
                      <span className="text-blue-500 mr-2">•</span>
                      <span className="text-sm text-gray-600">{item}</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          ))}
        </div>

        {/* Examples */}
        {currentHelpContent.examples && currentHelpContent.examples.length > 0 && (
          <div className="bg-gray-50 p-4 rounded-lg">
            <h4 className="font-semibold text-gray-900 mb-2">Examples:</h4>
            <ul className="space-y-1">
              {currentHelpContent.examples.map((example, index) => (
                <li key={index} className="flex items-start">
                  <span className="text-green-500 mr-2">→</span>
                  <span className="text-sm text-gray-600 italic">{example}</span>
                </li>
              ))}
            </ul>
          </div>
        )}

        {/* Close Button */}
        <div className="flex justify-end pt-4 border-t">
          <Button
            onClick={hideHelp}
            variant="primary"
            size="md"
          >
            Got it!
          </Button>
        </div>
      </div>
    </Modal>
  )
}
