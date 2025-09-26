import React, { createContext, useContext, useState, ReactNode } from 'react'
import { useAuth } from './AuthContext'

interface HelpContent {
  title: string
  description: string
  sections: {
    title: string
    content: string
    items?: string[]
  }[]
  examples?: string[]
}

interface HelpContextType {
  showHelp: (page: string) => void
  hideHelp: () => void
  isHelpVisible: boolean
  currentHelpContent: HelpContent | null
}

const HelpContext = createContext<HelpContextType | undefined>(undefined)

export const useHelp = () => {
  const context = useContext(HelpContext)
  if (!context) {
    throw new Error('useHelp must be used within a HelpProvider')
  }
  return context
}

// Help content for each page based on user role
const getHelpContent = (page: string, userRole: string): HelpContent => {
  const helpData: Record<string, Record<string, HelpContent>> = {
    'products': {
      ADMIN: {
        title: 'Products Management - Admin',
        description: 'Manage the product database and handle product requests from users.',
        sections: [
          {
            title: 'Search & Filter',
            content: 'Use the search bar to find specific products by name, or filter by category to narrow down results.',
            items: [
              'Type product name in search box',
              'Click category buttons to filter',
              'Use Low PHE filter for low-phenylalanine products'
            ]
          },
          {
            title: 'Product Management',
            content: 'As an admin, you can add new products directly to the database.',
            items: [
              'Click "Add Product" to create new product with full nutrition data',
              'Fill in all nutritional characteristics',
              'Products are immediately available to all users'
            ]
          },
          {
            title: 'CSV Upload',
            content: 'Bulk upload products using CSV files.',
            items: [
              'Go to Admin Panel for CSV upload functionality',
              'Download template to see required format',
              'Upload multiple products at once'
            ]
          }
        ],
        examples: [
          'Search for "apple" to find apple products',
          'Filter by "Fruits" category',
          'Add new product with complete nutrition data'
        ]
      },
      USER: {
        title: 'Products Database - User',
        description: 'Browse and search the product database to find suitable foods for your PKU diet.',
        sections: [
          {
            title: 'Search & Filter',
            content: 'Find products that fit your dietary needs.',
            items: [
              'Type product name in search box',
              'Click category buttons to filter by food type',
              'Use Low PHE filter to find safe products'
            ]
          },
          {
            title: 'Product Information',
            content: 'View detailed nutritional information for each product.',
            items: [
              'Click on any product to see full details',
              'Check PHE content per 100g',
              'View protein, calories, and other nutrients'
            ]
          },
          {
            title: 'Request New Products',
            content: 'Request products to be added to the database.',
            items: [
              'Submit product addition requests',
              'Provide source links and documentation',
              'Upload product photos for processed foods'
            ]
          }
        ],
        examples: [
          'Search for "rice" to find rice products',
          'Filter by "Grains" for cereal products',
          'Request new product if not found in database'
        ]
      },
      PATIENT: {
        title: 'Products Database - Patient',
        description: 'Browse and search the product database to find suitable foods for your PKU diet.',
        sections: [
          {
            title: 'Search & Filter',
            content: 'Find products that fit your dietary needs.',
            items: [
              'Type product name in search box',
              'Click category buttons to filter by food type',
              'Use Low PHE filter to find safe products'
            ]
          },
          {
            title: 'Product Information',
            content: 'View detailed nutritional information for each product.',
            items: [
              'Click on any product to see full details',
              'Check PHE content per 100g',
              'View protein, calories, and other nutrients'
            ]
          },
          {
            title: 'Request New Products',
            content: 'Request products to be added to the database.',
            items: [
              'Submit product addition requests',
              'Provide source links and documentation',
              'Upload product photos for processed foods'
            ]
          }
        ],
        examples: [
          'Search for "rice" to find rice products',
          'Filter by "Grains" for cereal products',
          'Request new product if not found in database'
        ]
      },
      HEALTHCARE_PROVIDER: {
        title: 'Products Database - Healthcare Provider',
        description: 'Access product database to help patients with PKU diet management.',
        sections: [
          {
            title: 'Product Research',
            content: 'Research products for patient recommendations.',
            items: [
              'Search for specific products by name',
              'Filter by nutritional characteristics',
              'View detailed nutrition facts and ingredients'
            ]
          },
          {
            title: 'Patient Support',
            content: 'Help patients find suitable products.',
            items: [
              'Recommend low-PHE products',
              'Check product safety for PKU patients',
              'Provide nutritional guidance'
            ]
          }
        ]
      }
    },
    'dishes': {
      ADMIN: {
        title: 'Dishes Management - Admin',
        description: 'Manage the dish database and review dish requests from users.',
        sections: [
          {
            title: 'Dish Management',
            content: 'Create and manage dishes in the common database.',
            items: [
              'Click "Add Dish" to create new dish',
              'Add ingredients and calculate nutrition',
              'Dishes are available to all users'
            ]
          },
          {
            title: 'Request Review',
            content: 'Review and approve dish requests from users.',
            items: [
              'Check Admin Panel for pending requests',
              'Review recipe details and nutrition calculations',
              'Approve or request modifications'
            ]
          }
        ]
      },
      USER: {
        title: 'Dishes - User',
        description: 'Create your own dishes and request additions to the common database.',
        sections: [
          {
            title: 'My Dishes',
            content: 'Create and manage your personal dish collection.',
            items: [
              'Click "Add Dish to My Dishes" to create personal dish',
              'Add ingredients and calculate nutrition',
              'Scale recipes for different serving sizes'
            ]
          },
          {
            title: 'Request to Common Database',
            content: 'Submit dishes to be added to the shared database.',
            items: [
              'Click "Request to Add Dish to Common Database"',
              'Provide detailed recipe and preparation method',
              'Include ingredient quantities and nutrition info'
            ]
          },
          {
            title: 'Browse Dishes',
            content: 'Find dishes by category and cuisine type.',
            items: [
              'Filter by Georgian, Ukrainian, or other cuisines',
              'Search by dish name or ingredients',
              'View nutrition information per serving'
            ]
          }
        ]
      },
      PATIENT: {
        title: 'Dishes - Patient',
        description: 'Create your own dishes and request additions to the common database.',
        sections: [
          {
            title: 'My Dishes',
            content: 'Create and manage your personal dish collection.',
            items: [
              'Click "Add Dish to My Dishes" to create personal dish',
              'Add ingredients and calculate nutrition',
              'Scale recipes for different serving sizes'
            ]
          },
          {
            title: 'Request to Common Database',
            content: 'Submit dishes to be added to the shared database.',
            items: [
              'Click "Request to Add Dish to Common Database"',
              'Provide detailed recipe and preparation method',
              'Include ingredient quantities and nutrition info'
            ]
          },
          {
            title: 'Browse Dishes',
            content: 'Find dishes by category and cuisine type.',
            items: [
              'Filter by Georgian, Ukrainian, or other cuisines',
              'Search by dish name or ingredients',
              'View nutrition information per serving'
            ]
          }
        ]
      },
      HEALTHCARE_PROVIDER: {
        title: 'Dishes - Healthcare Provider',
        description: 'Access dish database to help patients with meal planning.',
        sections: [
          {
            title: 'Dish Research',
            content: 'Research dishes for patient recommendations.',
            items: [
              'Search for specific dishes by name or ingredients',
              'Filter by cuisine type and dietary restrictions',
              'View detailed nutrition information'
            ]
          },
          {
            title: 'Patient Support',
            content: 'Help patients with meal planning.',
            items: [
              'Recommend suitable dishes for PKU patients',
              'Check dish safety and nutrition content',
              'Provide meal planning guidance'
            ]
          }
        ]
      }
    },
    'day': {
      ADMIN: {
        title: 'Day View - Admin',
        description: 'Monitor daily meal planning and nutrition tracking.',
        sections: [
          {
            title: 'Meal Management',
            content: 'View and manage daily meals and nutrition.',
            items: [
              'View all meal slots (breakfast, lunch, dinner, snack)',
              'Add products and dishes to meals',
              'Track daily PHE, protein, and calorie intake'
            ]
          },
          {
            title: 'Validation',
            content: 'Validate daily menu against PKU requirements.',
            items: [
              'Click "Validate Menu" to check daily intake',
              'Review warnings and recommendations',
              'Ensure PHE limits are not exceeded'
            ]
          }
        ]
      },
      USER: {
        title: 'Day View - User',
        description: 'Plan and track your daily meals and nutrition intake.',
        sections: [
          {
            title: 'Daily Planning',
            content: 'Plan your meals for the day.',
            items: [
              'Add products and dishes to each meal',
              'Track quantities and serving sizes',
              'Monitor daily PHE, protein, and calorie intake'
            ]
          },
          {
            title: 'Meal Management',
            content: 'Manage your daily meals.',
            items: [
              'Click + Product to add individual products',
              'Click + Dish to add complete dishes',
              'Mark items as consumed when eaten'
            ]
          },
          {
            title: 'Snack Suggestions',
            content: 'Get suggestions for low-PHE snacks.',
            items: [
              'Click "Show Suggestions" for snack ideas',
              'Add suggested snacks to your meals',
              'Based on your remaining daily PHE allowance'
            ]
          },
          {
            title: 'Menu Validation',
            content: 'Check if your daily menu meets PKU requirements.',
            items: [
              'Click "Validate Menu" to check your daily intake',
              'Review any warnings or recommendations',
              'Adjust meals if PHE limits are exceeded'
            ]
          }
        ]
      },
      PATIENT: {
        title: 'Day View - Patient',
        description: 'Plan and track your daily meals and nutrition intake.',
        sections: [
          {
            title: 'Daily Planning',
            content: 'Plan your meals for the day.',
            items: [
              'Add products and dishes to each meal',
              'Track quantities and serving sizes',
              'Monitor daily PHE, protein, and calorie intake'
            ]
          },
          {
            title: 'Meal Management',
            content: 'Manage your daily meals.',
            items: [
              'Click + Product to add individual products',
              'Click + Dish to add complete dishes',
              'Mark items as consumed when eaten'
            ]
          },
          {
            title: 'Snack Suggestions',
            content: 'Get suggestions for low-PHE snacks.',
            items: [
              'Click "Show Suggestions" for snack ideas',
              'Add suggested snacks to your meals',
              'Based on your remaining daily PHE allowance'
            ]
          },
          {
            title: 'Menu Validation',
            content: 'Check if your daily menu meets PKU requirements.',
            items: [
              'Click "Validate Menu" to check your daily intake',
              'Review any warnings or recommendations',
              'Adjust meals if PHE limits are exceeded'
            ]
          }
        ]
      },
      HEALTHCARE_PROVIDER: {
        title: 'Day View - Healthcare Provider',
        description: 'Monitor patient daily meal planning and nutrition tracking.',
        sections: [
          {
            title: 'Patient Monitoring',
            content: 'Monitor patient daily meal planning.',
            items: [
              'View patient meal plans and nutrition intake',
              'Check PHE, protein, and calorie consumption',
              'Identify potential dietary issues'
            ]
          },
          {
            title: 'Nutritional Guidance',
            content: 'Provide nutritional guidance to patients.',
            items: [
              'Review patient menu validation results',
              'Suggest meal adjustments if needed',
              'Recommend suitable products and dishes'
            ]
          }
        ]
      }
    },
    'week': {
      ADMIN: {
        title: 'Week View - Admin',
        description: 'Monitor weekly meal planning and generation.',
        sections: [
          {
            title: 'Week Management',
            content: 'View and manage weekly meal plans.',
            items: [
              'View 7-day calendar with meal plans',
              'Generate weekly menus automatically',
              'Review nutrition statistics'
            ]
          },
          {
            title: 'Generation Options',
            content: 'Configure menu generation settings.',
            items: [
              'Enable Emergency Mode for urgent planning',
              'Respect Pantry to use available ingredients',
              'Set generation rules and preferences'
            ]
          }
        ]
      },
      USER: {
        title: 'Week View - User',
        description: 'Plan your weekly meals and generate menu suggestions.',
        sections: [
          {
            title: 'Weekly Planning',
            content: 'Plan your meals for the entire week.',
            items: [
              'View 7-day calendar with your meal plans',
              'Generate weekly menus automatically',
              'See nutrition statistics for the week'
            ]
          },
          {
            title: 'Menu Generation',
            content: 'Use AI to generate meal suggestions.',
            items: [
              'Click "Generate Week" for full week planning',
              'Click "Generate Day" for single day',
              'Enable Emergency Mode if needed'
            ]
          },
          {
            title: 'Generation Rules',
            content: 'Configure how menus are generated.',
            items: [
              'Emergency Mode: allows dish repeats within 2 days',
              'Respect Pantry: uses ingredients you already have',
              'Nutrition: targets daily PHE limits and balance'
            ]
          }
        ]
      },
      PATIENT: {
        title: 'Week View - Patient',
        description: 'Plan your weekly meals and generate menu suggestions.',
        sections: [
          {
            title: 'Weekly Planning',
            content: 'Plan your meals for the entire week.',
            items: [
              'View 7-day calendar with your meal plans',
              'Generate weekly menus automatically',
              'See nutrition statistics for the week'
            ]
          },
          {
            title: 'Menu Generation',
            content: 'Use AI to generate meal suggestions.',
            items: [
              'Click "Generate Week" for full week planning',
              'Click "Generate Day" for single day',
              'Enable Emergency Mode if needed'
            ]
          },
          {
            title: 'Generation Rules',
            content: 'Configure how menus are generated.',
            items: [
              'Emergency Mode: allows dish repeats within 2 days',
              'Respect Pantry: uses ingredients you already have',
              'Nutrition: targets daily PHE limits and balance'
            ]
          }
        ]
      },
      HEALTHCARE_PROVIDER: {
        title: 'Week View - Healthcare Provider',
        description: 'Monitor patient weekly meal planning and generation.',
        sections: [
          {
            title: 'Patient Monitoring',
            content: 'Monitor patient weekly meal planning.',
            items: [
              'View patient weekly meal plans',
              'Check nutrition statistics and trends',
              'Identify potential dietary issues'
            ]
          },
          {
            title: 'Nutritional Guidance',
            content: 'Provide nutritional guidance to patients.',
            items: [
              'Review patient menu generation settings',
              'Suggest meal planning improvements',
              'Recommend suitable dishes and products'
            ]
          }
        ]
      }
    },
    'pantry': {
      ADMIN: {
        title: 'Pantry Management - Admin',
        description: 'Monitor pantry usage and budget settings.',
        sections: [
          {
            title: 'Pantry Overview',
            content: 'View pantry inventory and usage patterns.',
            items: [
              'Monitor product quantities and expiry dates',
              'Track price information and budget usage',
              'View pantry statistics and trends'
            ]
          }
        ]
      },
      USER: {
        title: 'Pantry - User',
        description: 'Manage your food inventory and budget.',
        sections: [
          {
            title: 'Inventory Management',
            content: 'Keep track of your food supplies.',
            items: [
              'Add products to your pantry',
              'Set quantities and expiry dates',
              'Track prices for budget planning'
            ]
          },
          {
            title: 'Budget Settings',
            content: 'Set and manage your food budget.',
            items: [
              'Set daily, weekly, and monthly budgets',
              'Choose currency for price tracking',
              'Enable budget constraints for meal generation'
            ]
          },
          {
            title: 'Smart Features',
            content: 'Use pantry data for better meal planning.',
            items: [
              'Expiring items are highlighted',
              'Menu generation can use pantry items first',
              'Track spending and budget compliance'
            ]
          }
        ]
      },
      PATIENT: {
        title: 'Pantry - Patient',
        description: 'Manage your food inventory and budget.',
        sections: [
          {
            title: 'Inventory Management',
            content: 'Keep track of your food supplies.',
            items: [
              'Add products to your pantry',
              'Set quantities and expiry dates',
              'Track prices for budget planning'
            ]
          },
          {
            title: 'Budget Settings',
            content: 'Set and manage your food budget.',
            items: [
              'Set daily, weekly, and monthly budgets',
              'Choose currency for price tracking',
              'Enable budget constraints for meal generation'
            ]
          },
          {
            title: 'Smart Features',
            content: 'Use pantry data for better meal planning.',
            items: [
              'Expiring items are highlighted',
              'Menu generation can use pantry items first',
              'Track spending and budget compliance'
            ]
          }
        ]
      },
      HEALTHCARE_PROVIDER: {
        title: 'Pantry - Healthcare Provider',
        description: 'Monitor patient pantry management and budget settings.',
        sections: [
          {
            title: 'Patient Monitoring',
            content: 'Monitor patient pantry management.',
            items: [
              'View patient pantry inventory',
              'Check budget settings and spending',
              'Identify potential dietary issues'
            ]
          },
          {
            title: 'Nutritional Guidance',
            content: 'Provide nutritional guidance to patients.',
            items: [
              'Review patient pantry items for safety',
              'Suggest budget-friendly meal planning',
              'Recommend suitable products for pantry'
            ]
          }
        ]
      }
    },
    'scan': {
      ADMIN: {
        title: 'Product Scanning - Admin',
        description: 'Scan product labels and manage submissions.',
        sections: [
          {
            title: 'Label Scanning',
            content: 'Scan product labels to extract nutrition information.',
            items: [
              'Upload clear photos of product labels',
              'Include nutrition facts and ingredients list',
              'Review extracted data for accuracy'
            ]
          },
          {
            title: 'Data Management',
            content: 'Manage scanned product data.',
            items: [
              'Review and approve user submissions',
              'Add verified products to database',
              'Monitor scanning accuracy and quality'
            ]
          }
        ]
      },
      USER: {
        title: 'Product Scanning - User',
        description: 'Scan product labels to get nutrition information.',
        sections: [
          {
            title: 'How to Scan',
            content: 'Get the best results from label scanning.',
            items: [
              'Upload clear photos of product labels',
              'Include nutrition facts and ingredients list',
              'Take multiple angles for better accuracy'
            ]
          },
          {
            title: 'Scanning Tips',
            content: 'Tips for better scanning results.',
            items: [
              'Ensure good lighting and avoid glare',
              'Focus on nutrition facts and ingredients',
              'Capture front label, nutrition facts, and ingredients'
            ]
          },
          {
            title: 'Submit to Database',
            content: 'Contribute to the global product database.',
            items: [
              'Review extracted information for accuracy',
              'Submit verified products to global catalog',
              'Help improve the database for everyone'
            ]
          }
        ]
      },
      PATIENT: {
        title: 'Product Scanning - Patient',
        description: 'Scan product labels to get nutrition information.',
        sections: [
          {
            title: 'How to Scan',
            content: 'Get the best results from label scanning.',
            items: [
              'Upload clear photos of product labels',
              'Include nutrition facts and ingredients list',
              'Take multiple angles for better accuracy'
            ]
          },
          {
            title: 'Scanning Tips',
            content: 'Tips for better scanning results.',
            items: [
              'Ensure good lighting and avoid glare',
              'Focus on nutrition facts and ingredients',
              'Capture front label, nutrition facts, and ingredients'
            ]
          },
          {
            title: 'Submit to Database',
            content: 'Contribute to the global product database.',
            items: [
              'Review extracted information for accuracy',
              'Submit verified products to global catalog',
              'Help improve the database for everyone'
            ]
          }
        ]
      },
      HEALTHCARE_PROVIDER: {
        title: 'Product Scanning - Healthcare Provider',
        description: 'Scan product labels to help patients with nutrition information.',
        sections: [
          {
            title: 'Patient Support',
            content: 'Help patients scan product labels.',
            items: [
              'Guide patients through scanning process',
              'Review scanned nutrition information',
              'Verify product safety for PKU patients'
            ]
          },
          {
            title: 'Database Contribution',
            content: 'Contribute to the global product database.',
            items: [
              'Submit verified products to global catalog',
              'Review patient submissions for accuracy',
              'Help improve the database for everyone'
            ]
          }
        ]
      }
    },
    'adminPanel': {
      ADMIN: {
        title: 'Admin Panel',
        description: 'Central hub for system management and request review.',
        sections: [
          {
            title: 'Pending Requests',
            content: 'Review and manage user requests.',
            items: [
              'Product Requests: Review new product submissions',
              'Dish Requests: Review new dish submissions',
              'Approve or request modifications'
            ]
          },
          {
            title: 'CSV Upload',
            content: 'Bulk upload data using CSV files.',
            items: [
              'Upload Products CSV: Bulk import products',
              'Upload Dishes CSV: Bulk import dishes',
              'Download templates for correct format'
            ]
          },
          {
            title: 'System Management',
            content: 'Manage the overall system.',
            items: [
              'Monitor user activity and requests',
              'Manage system settings and configurations',
              'Review and moderate content'
            ]
          }
        ]
      },
      USER: {
        title: 'Admin Panel - User',
        description: 'This page is only accessible to administrators.',
        sections: [
          {
            title: 'Access Denied',
            content: 'You do not have permission to access this page.',
            items: [
              'This page is restricted to administrators only',
              'Contact your administrator if you need access',
              'Return to your dashboard to continue using the app'
            ]
          }
        ]
      },
      PATIENT: {
        title: 'Admin Panel - Patient',
        description: 'This page is only accessible to administrators.',
        sections: [
          {
            title: 'Access Denied',
            content: 'You do not have permission to access this page.',
            items: [
              'This page is restricted to administrators only',
              'Contact your administrator if you need access',
              'Return to your dashboard to continue using the app'
            ]
          }
        ]
      },
      HEALTHCARE_PROVIDER: {
        title: 'Admin Panel - Healthcare Provider',
        description: 'This page is only accessible to administrators.',
        sections: [
          {
            title: 'Access Denied',
            content: 'You do not have permission to access this page.',
            items: [
              'This page is restricted to administrators only',
              'Contact your administrator if you need access',
              'Return to your dashboard to continue using the app'
            ]
          }
        ]
      }
    }
  }

  const pageHelp = helpData[page]?.[userRole]
  
  if (pageHelp) {
    return pageHelp
  }
  return {
    title: `${page.charAt(0).toUpperCase() + page.slice(1)} Help`,
    description: `This page helps you manage your PKU diet. You are viewing the ${page} page as a ${userRole.toLowerCase()}.`,
    sections: [{
      title: 'General Usage',
      content: 'Use this page to manage your dietary needs and track your nutrition intake.',
      items: ['Browse available options', 'Add items to your plan', 'Track your progress']
    }]
  }
}

interface HelpProviderProps {
  children: ReactNode
}

export const HelpProvider: React.FC<HelpProviderProps> = ({ children }) => {
  const [isHelpVisible, setIsHelpVisible] = useState(false)
  const [currentPage, setCurrentPage] = useState<string | null>(null)
  const { user } = useAuth()

  const showHelp = (page: string) => {
    setCurrentPage(page)
    setIsHelpVisible(true)
  }

  const hideHelp = () => {
    setIsHelpVisible(false)
    setCurrentPage(null)
  }

  const currentHelpContent = currentPage ? getHelpContent(currentPage, user?.role || 'user') : null

  return (
    <HelpContext.Provider value={{
      showHelp,
      hideHelp,
      isHelpVisible,
      currentHelpContent
    }}>
      {children}
    </HelpContext.Provider>
  )
}
